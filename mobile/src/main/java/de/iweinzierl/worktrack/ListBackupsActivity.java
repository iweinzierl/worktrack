package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.BackupHelper;
import de.iweinzierl.worktrack.view.adapter.BackupAdapter;
import de.iweinzierl.worktrack.view.dialog.AuthenticationDialogFragment;
import de.iweinzierl.worktrack.view.dialog.BackupTitleInputDialog;

@EActivity
public class ListBackupsActivity extends BaseGoogleApiActivity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(ListBackupsActivity.class.getName());

    private final BackupHelper.BackupCallback backupCallback = new BackupHelper.DefaultBackupCallback() {
        @Override
        public void onCreationSuccessful() {
            showMessage("Backup creation successful");
        }

        @Override
        public void onCreationFailed() {
            showMessage("Backup creation successful");
        }

        @Override
        public void onImportFailed() {
            showMessage("Backup import failed");
        }

        @Override
        public void onImportSuccessful() {
            showMessage("Backup import successful");
        }

        @Override
        public void onListBackups(List<BackupMetaData> backups) {
            setBackups(backups);
        }
    };

    private BackupAdapter backupAdapter;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    RecyclerView backups;

    @ViewById
    Toolbar toolbar;

    @Override
    int getLayoutId() {
        return R.layout.activity_list_backups;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupAdapter();
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

    @AfterViews
    void setupViews() {
        setupAdapter();

        backups.setAdapter(backupAdapter);
        backups.setHasFixedSize(false);
        backups.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setTitle(R.string.activity_list_backups);
        setSupportActionBar(toolbar);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        updateBackups();
    }

    @Background
    protected void updateBackups() {
        LOGGER.info("Start updating backup list Google Drive");

        new BackupHelper(
                backupCallback,
                trackingItemRepository,
                getGoogleApiClient()).listBackups();
    }

    @UiThread
    protected void setBackups(List<BackupMetaData> backupMetaDataList) {
        LOGGER.info("Found {} backups in Google Drive App folder", backupMetaDataList.size());
        backupAdapter.setItems(backupMetaDataList);
    }

    @UiThread
    protected void createBackup() {
        final BackupTitleInputDialog dialog = new BackupTitleInputDialog();

        dialog.setCallback(new BackupTitleInputDialog.Callback() {
            @Override
            public void onConfirm(String title) {
                createBackup(title);
            }

            @Override
            public void onCancel() {
                // nothing to do
            }
        });

        dialog.show(getSupportFragmentManager(), null);
    }

    @Background
    protected void createBackup(String title) {
        new BackupHelper(
                backupCallback,
                trackingItemRepository,
                getGoogleApiClient()).writeBackup(title);
    }

    @UiThread
    protected void importBackup(final BackupMetaData backupMetaData) {
        AuthenticationDialogFragment dialogFragment = new AuthenticationDialogFragment();
        dialogFragment.setCallback(new AuthenticationDialogFragment.Callback() {
            @Override
            public void onAuthenticationSucceeded() {
                LOGGER.info("Import Backup: {}", backupMetaData);
                importBackup(backupMetaData.getDriveId());
            }

            @Override
            public void onAuthenticationFailed() {
            }

            @Override
            public void onAuthenticationCancelled() {
            }
        });
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @Background
    protected void importBackup(String backupDriveId) {
        new BackupHelper(
                backupCallback,
                trackingItemRepository,
                getGoogleApiClient()
        ).importBackup(backupDriveId);
    }
}