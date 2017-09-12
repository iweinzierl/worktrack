package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.model.Backup;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.LocalWorkplaceRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.WorkplaceRepository;
import de.iweinzierl.worktrack.util.BackupHelper;
import de.iweinzierl.worktrack.util.GoogleDriveBackupHelper;
import de.iweinzierl.worktrack.util.ItemTouchHelperCallback;
import de.iweinzierl.worktrack.view.adapter.BackupAdapter;
import de.iweinzierl.worktrack.view.dialog.AuthenticationDialogFragment;
import de.iweinzierl.worktrack.view.dialog.BackupTitleInputDialog;

@EActivity
public class ListBackupsActivity extends BaseGoogleApiAvailabilityActivity implements ItemTouchHelperCallback.ItemCallback<BackupMetaData> {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(ListBackupsActivity.class.getName());

    private BackupAdapter backupAdapter;

    private FirebaseAnalytics analytics;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @Bean(LocalWorkplaceRepository.class)
    WorkplaceRepository workplaceRepository;

    @ViewById
    RecyclerView backups;

    @ViewById
    Toolbar toolbar;

    @ViewById
    protected ProgressBar progressBar;

    @Override
    int getLayoutId() {
        return R.layout.activity_list_backups;
    }

    @Override
    void onConnected() {
        //updateBackups();
    }

    @Override
    void onFailure() {
        showMessage("Authentication Error!");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = FirebaseAnalytics.getInstance(this);
        setupAdapter();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateBackups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_backups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_backup:
                createBackup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDeleteItem(final BackupMetaData item) {
        deleteBackup(item);
    }

    @AfterViews
    void setupViews() {
        setupAdapter();

        backups.setAdapter(backupAdapter);
        backups.setHasFixedSize(false);
        backups.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setTitle(R.string.activity_list_backups);
        setSupportActionBar(toolbar);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback<>(this, backupAdapter, this));
        touchHelper.attachToRecyclerView(backups);
    }

    @UiThread
    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @UiThread
    protected void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setupAdapter() {
        if (backupAdapter == null) {
            backupAdapter = new BackupAdapter(new BackupAdapter.ClickCallback() {
                @Override
                public void onClick(int position, BackupMetaData backupMetaData) {
                    importBackup(backupMetaData);
                }
            });
        }
    }

    @UiThread
    protected void setBackups(List<BackupMetaData> backupMetaDataList) {
        LOGGER.info("Found {} backups in Google Drive App folder", backupMetaDataList.size());
        backupAdapter.setItems(backupMetaDataList);
    }

    @UiThread
    protected void addBackup(BackupMetaData backupMetaData) {
        LOGGER.info("Add further backup to backup list: {}", backupMetaData.getDriveId());
        backupAdapter.addItem(backupMetaData);
    }

    @UiThread
    protected void removeBackup(BackupMetaData backupMetaData) {
        LOGGER.info("Remove backup from backup list: {}", backupMetaData.getDriveId());
        backupAdapter.removeItem(backupMetaData);
    }

    @UiThread
    protected void createBackup() {
        final BackupTitleInputDialog dialog = new BackupTitleInputDialog();

        dialog.setCallback(new BackupTitleInputDialog.Callback() {
            @Override
            public void onConfirm(String title) {
                showProgressBar();
                createBackup(title);
            }

            @Override
            public void onCancel() {
                // nothing to do
            }
        });

        dialog.show(getSupportFragmentManager(), null);
    }

    @UiThread
    protected void importBackup(final BackupMetaData backupMetaData) {
        showProgressBar();

        final AuthenticationDialogFragment dialogFragment = new AuthenticationDialogFragment();
        dialogFragment.setCallback(new AuthenticationDialogFragment.Callback() {
            @Override
            public void onAuthenticationSucceeded() {
                LOGGER.info("Import Backup: {}", backupMetaData);
                importBackup(backupMetaData.getDriveId());
                dialogFragment.dismiss();
            }

            @Override
            public void onAuthenticationFailed() {
            }

            @Override
            public void onAuthenticationCancelled() {
                dialogFragment.dismiss();
            }
        });
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @Background
    protected void updateBackups() {
        LOGGER.info("Start updating backup list Google Drive");

        showProgressBar();

        executeAction(new BaseGoogleApiAvailabilityActivity.Action() {
            @Override
            public void execute() {
                try {
                    GoogleDriveBackupHelper googleDriveBackupHelper = new GoogleDriveBackupHelper(
                            ListBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.listBackups(new GoogleDriveBackupHelper.Callback<List<BackupMetaData>>() {
                        @Override
                        public void onSuccess(List<BackupMetaData> result) {
                            setBackups(result);
                            hideProgressBar();
                        }
                    });
                } catch (IOException e) {
                    LOGGER.error("Error while loading backups", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_list_backups_error_loading_backups));

                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
                    analytics.logEvent(AnalyticsEvents.BACKUP_LOADING_FAILED.name(), bundle);
                }
            }
        });
    }

    @Background
    protected void createBackup(final String title) {
        LOGGER.info("Start creating backup for Google Drive");

        executeAction(new BaseGoogleApiAvailabilityActivity.Action() {
            @Override
            public void execute() {
                try {
                    GoogleDriveBackupHelper googleDriveBackupHelper = new GoogleDriveBackupHelper(
                            ListBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.createBackup(
                            title,
                            trackingItemRepository.findAll(),
                            workplaceRepository.findAll(),
                            new GoogleDriveBackupHelper.Callback<BackupMetaData>() {
                                @Override
                                public void onSuccess(BackupMetaData result) {
                                    addBackup(result);
                                    hideProgressBar();
                                }
                            });
                } catch (IOException e) {
                    LOGGER.error("Error while loading backups", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_list_backups_error_loading_backups));

                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
                    analytics.logEvent(AnalyticsEvents.BACKUP_LOADING_FAILED.name(), bundle);
                }
            }
        });
    }

    @Background
    protected void importBackup(final String backupDriveId) {
        showProgressBar();

        executeAction(new Action() {
            @Override
            public void execute() {
                try {
                    GoogleDriveBackupHelper googleDriveBackupHelper = new GoogleDriveBackupHelper(
                            ListBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.getBackup(backupDriveId, new GoogleDriveBackupHelper.Callback<Backup>() {
                        @Override
                        public void onSuccess(Backup result) {
                            new BackupHelper(trackingItemRepository, workplaceRepository)
                                    .importBackup(result, new BackupHelper.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            showMessage(getString(R.string.activity_list_backups_import_backup_succeeded));
                                            hideProgressBar();
                                        }

                                        @Override
                                        public void onFailure() {
                                            showMessage(getString(R.string.activity_list_backups_error_importing_backup));
                                            hideProgressBar();
                                        }
                                    });
                        }
                    });
                } catch (IOException e) {
                    LOGGER.error("Error while importing backup", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_list_backups_error_importing_backup));

                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
                    analytics.logEvent(AnalyticsEvents.BACKUP_IMPORT_FAILED.name(), bundle);
                }
            }
        });
    }

    @Background
    public void deleteBackup(final BackupMetaData item) {
        showProgressBar();

        executeAction(new Action() {
            @Override
            public void execute() {
                try {
                    new GoogleDriveBackupHelper(ListBackupsActivity.this, getCredential())
                            .deleteBackup(item.getDriveId(), new GoogleDriveBackupHelper.Callback<String>() {
                                @Override
                                public void onSuccess(String result) {
                                    removeBackup(item);
                                    hideProgressBar();
                                }
                            });
                } catch (IOException e) {
                    LOGGER.error("Error while loading backups", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_list_backups_error_deleting_backup));

                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
                    analytics.logEvent(AnalyticsEvents.BACKUP_DELETION_FAILED.name(), bundle);
                }
            }
        });
    }
}
