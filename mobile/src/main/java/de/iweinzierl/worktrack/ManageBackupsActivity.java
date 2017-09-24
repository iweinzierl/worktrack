package de.iweinzierl.worktrack;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
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
import de.iweinzierl.worktrack.view.adapter.BackupAdapter;
import de.iweinzierl.worktrack.view.adapter.NoOpActionCallback;
import de.iweinzierl.worktrack.view.dialog.BackupTitleInputDialog;

@EActivity
public class ManageBackupsActivity extends BaseGoogleApiAvailabilityActivity {

    private class BackupsActionCallback extends NoOpActionCallback<BackupMetaData> {
        @Override
        public void onSelectItem(BackupMetaData item) {
            importBackupWithConfirmation(item);
        }

        @Override
        public void onDeleteItem(BackupMetaData item) {
            removeBackupWithConfirmation(item);
        }
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(ManageBackupsActivity.class.getName());

    private static final int REQUEST_GOOGLE_DRIVE_LIST_BACKUPS = 1001;

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
    void onFailure(int errorCode) {
        if (errorCode == ERROR_UNDEFINED_ACCOUNT) {
            displayAccountUnsetDialog();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = FirebaseAnalytics.getInstance(this);
        setupAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBackups();
    }

    @AfterViews
    void setupViews() {
        setupAdapter();

        backups.setAdapter(backupAdapter);
        backups.setHasFixedSize(false);
        backups.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setTitle(R.string.activity_manage_backups);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GOOGLE_DRIVE_LIST_BACKUPS && resultCode == RESULT_OK) {
            updateBackups();
        }
    }

    @Click(R.id.manualBackup)
    protected void clickedManualBack() {
        createBackup();
    }

    @UiThread
    protected void displayAccountUnsetDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_manage_backups_dialog_error_account_unset_title)
                .setMessage(R.string.activity_manage_backups_dialog_error_account_unset_message)
                .setNegativeButton(R.string.activity_manage_backups_dialog_error_account_unset_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        hideProgressBar();
                    }
                })
                .setPositiveButton(R.string.activity_manage_backups_dialog_error_account_unset_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(ManageBackupsActivity.this, SettingsActivity_.class));
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        hideProgressBar();
                    }
                })
                .show();
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
            backupAdapter = new BackupAdapter(new BackupsActionCallback());
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
    protected void removeBackupWithConfirmation(final BackupMetaData backupMetaData) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_manage_backups_dialog_discard_title)
                .setNegativeButton(R.string.util_delete_action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        hideProgressBar();
                    }
                })
                .setPositiveButton(R.string.util_delete_action_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        deleteBackup(backupMetaData);
                        hideProgressBar();
                    }
                }).show();
    }

    @UiThread
    protected void removeBackup(BackupMetaData backupMetaData) {
        LOGGER.info("Remove backup from backup list: {}", backupMetaData.getDriveId());
        backupAdapter.removeItem(backupMetaData);
        showMessage(getString(R.string.activity_manage_backups_discard_backup_succeeded));
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
    protected void importBackupWithConfirmation(final BackupMetaData backupMetaData) {
        showProgressBar();

        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_manage_backups_dialog_import_title)
                .setNegativeButton(R.string.util_import_action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        hideProgressBar();
                    }
                })
                .setPositiveButton(R.string.util_import_action_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        importBackup(backupMetaData.getDriveId());
                    }
                }).show();
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
                            ManageBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.listBackups(new GoogleDriveBackupHelper.Callback<List<BackupMetaData>>() {
                        @Override
                        public void onSuccess(List<BackupMetaData> result) {
                            setBackups(result);
                            hideProgressBar();
                        }
                    });
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_GOOGLE_DRIVE_LIST_BACKUPS);
                } catch (IOException e) {
                    LOGGER.error("Error while loading backups", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_manage_backups_error_loading_backups));

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
                            ManageBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.createBackup(
                            title,
                            trackingItemRepository.findAll(),
                            workplaceRepository.findAll(),
                            new GoogleDriveBackupHelper.Callback<BackupMetaData>() {
                                @Override
                                public void onSuccess(BackupMetaData result) {
                                    addBackup(result);
                                    showMessage(getString(R.string.activity_manage_backups_create_backup_succeeded));
                                    hideProgressBar();
                                }
                            });
                } catch (IOException e) {
                    LOGGER.error("Error while loading backups", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_manage_backups_error_loading_backups));

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
                            ManageBackupsActivity.this, getCredential());

                    googleDriveBackupHelper.getBackup(backupDriveId, new GoogleDriveBackupHelper.Callback<Backup>() {
                        @Override
                        public void onSuccess(Backup result) {
                            new BackupHelper(trackingItemRepository, workplaceRepository)
                                    .importBackup(result, new BackupHelper.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            showMessage(getString(R.string.activity_manage_backups_import_backup_succeeded));
                                            hideProgressBar();
                                        }

                                        @Override
                                        public void onFailure() {
                                            showMessage(getString(R.string.activity_manage_backups_error_importing_backup));
                                            hideProgressBar();
                                        }
                                    });
                        }
                    });
                } catch (IOException e) {
                    LOGGER.error("Error while importing backup", e);
                    hideProgressBar();
                    showMessage(getString(R.string.activity_manage_backups_error_importing_backup));

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
                    new GoogleDriveBackupHelper(ManageBackupsActivity.this, getCredential())
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
                    showMessage(getString(R.string.activity_manage_backups_error_deleting_backup));

                    Bundle bundle = new Bundle();
                    bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
                    analytics.logEvent(AnalyticsEvents.BACKUP_DELETION_FAILED.name(), bundle);
                }
            }
        });
    }
}
