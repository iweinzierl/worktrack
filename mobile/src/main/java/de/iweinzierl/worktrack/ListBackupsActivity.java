package de.iweinzierl.worktrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

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

@EActivity(R.layout.activity_list_backups)
public class ListBackupsActivity extends BaseGoogleApiActivity {

    public static final String EXTRA_BACKUP_DRIVE_ID = "ListBackupsActivity.Extra.BackupId";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(ListBackupsActivity.class.getName());

    private final BackupHelper.BackupCallback backupCallback = new BackupHelper.DefaultBackupCallback() {
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backupAdapter = new BackupAdapter(new BackupAdapter.ClickCallback() {
            @Override
            public void onClick(int position, BackupMetaData backupMetaData) {
                importBackup(backupMetaData);
            }
        });
    }

    @AfterViews
    void setupViews() {
        backups.setAdapter(backupAdapter);
        backups.setHasFixedSize(false);
        backups.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setTitle(R.string.activity_list_backups);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        updateBackups();
    }

    @Background
    protected void updateBackups() {
        LOGGER.info("Start updating writeBackup list Google Drive");

        new BackupHelper(backupCallback, trackingItemRepository, getGoogleApiClient()).listBackups();
    }

    @UiThread
    protected void setBackups(List<BackupMetaData> backupMetaDataList) {
        LOGGER.info("Found {} backups in Google Drive App folder", backupMetaDataList.size());
        backupAdapter.setItems(backupMetaDataList);
    }

    @UiThread
    protected void importBackup(final BackupMetaData backupMetaData) {
        AuthenticationDialogFragment dialogFragment = new AuthenticationDialogFragment();
        dialogFragment.setCallback(new AuthenticationDialogFragment.Callback() {
            @Override
            public void onAuthenticationSucceeded() {
                LOGGER.info("Import writeBackup: {}", backupMetaData);

                Intent data = new Intent();
                data.putExtra(EXTRA_BACKUP_DRIVE_ID, backupMetaData.getDriveId());

                setResult(RESULT_OK, data);
                finish();
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
}
