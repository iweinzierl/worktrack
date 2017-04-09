package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.iweinzierl.worktrack.model.Backup;
import de.iweinzierl.worktrack.view.adapter.BackupAdapter;

@EActivity(R.layout.activity_list_backups)
public class ListBackupsActivity extends BaseGoogleApiActivity {

    public static final String EXTRA_BACKUP_DRIVE_ID = "ListBackupsActivity.Extra.BackupId";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(ListBackupsActivity.class.getName());

    private BackupAdapter backupAdapter = new BackupAdapter();

    @ViewById
    RecyclerView backups;

    @ViewById
    Toolbar toolbar;

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
        LOGGER.info("Start updating backup list Google Drive");

        final DriveFolder appFolder = Drive.DriveApi.getAppFolder(getGoogleApiClient());

        PendingResult<DriveApi.MetadataBufferResult> metadataBufferResultPendingResult = appFolder.listChildren(getGoogleApiClient());
        metadataBufferResultPendingResult.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                final List<Backup> backupList = new ArrayList<>();

                for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                    backupList.add(new Backup(
                            metadata.getDriveId().encodeToString(),
                            metadata.getTitle(),
                            metadata.getFileSize(),
                            new LocalDateTime(metadata.getModifiedDate().getTime())
                    ));
                }

                setBackups(backupList);
            }
        });
    }

    @UiThread
    protected void setBackups(List<Backup> backupList) {
        LOGGER.info("Found {} backups in Google Drive App folder", backupList.size());
        backupAdapter.setItems(backupList);
    }
}
