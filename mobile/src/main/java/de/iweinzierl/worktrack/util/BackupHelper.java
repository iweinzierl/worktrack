package de.iweinzierl.worktrack.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Objects;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;

public class BackupHelper {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupHelper.class.getName());

    private static final String MIME_TYPE = "text/plain";
    private static final String BACKUP_ENDING = ".csv";
    private static final char SEPARATOR = ',';
    private static final char QUOTE_CHARACTER = '"';

    private final Activity activity;
    private final TrackingItemRepository trackingItemRepository;
    private final GoogleApiClient googleApiClient;

    private final ResultCallback<DriveFolder.DriveFileResult> creationCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }
            showMessage("Created Backup in App Folder: " + result.getDriveFile().getDriveId());
        }
    };

    private final DriveFile.DownloadProgressListener progressListener = new DriveFile.DownloadProgressListener() {
        @Override
        public void onProgress(long bytesDownloaded, long bytesExpected) {
            // TODO update progress bar
        }
    };

    public BackupHelper(Activity activity, TrackingItemRepository trackingItemRepository, GoogleApiClient googleApiClient) {
        Objects.requireNonNull(activity, "activity must not be null");
        Objects.requireNonNull(trackingItemRepository, "trackingItemRepository must not be null");
        Objects.requireNonNull(googleApiClient, "googleApiClient must not be null");

        this.activity = activity;
        this.trackingItemRepository = trackingItemRepository;
        this.googleApiClient = googleApiClient;
    }

    public void backup(@NonNull final String title) {
        final String backupTitle = title.endsWith(BACKUP_ENDING) ? title : title + BACKUP_ENDING;

        Drive.DriveApi
                .newDriveContents(googleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            showMessage("Error while trying to create new file contents");
                            return;
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(backupTitle)
                                .setMimeType(MIME_TYPE)
                                .build();

                        try {
                            createBackup(result.getDriveContents().getOutputStream());

                            Drive.DriveApi.getAppFolder(googleApiClient)
                                    .createFile(googleApiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(creationCallback);
                        } catch (IOException e) {
                            LOGGER.error("Unable to create backup", e);
                        }
                    }
                });
    }

    public void importBackup(@NonNull final String backupDriveId) {
        Objects.requireNonNull(backupDriveId, "backupDriveId must not be null");

        Drive.DriveApi
                .fetchDriveId(googleApiClient, backupDriveId)
                .setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                        driveIdResult.getDriveId().asDriveFile()
                                .open(googleApiClient, DriveFile.MODE_READ_ONLY, progressListener)
                                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                                        try {
                                            importBackup(driveContentsResult.getDriveContents().getInputStream());
                                        } catch (IOException e) {
                                            LOGGER.error("Unable to import backup", e);
                                        }
                                    }
                                });
                    }
                });
    }

    public void cleanBackups() {
        Drive.DriveApi.getAppFolder(googleApiClient).listChildren(googleApiClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                    LOGGER.info("Delete -> {}", metadata.getTitle());
                    metadata.getDriveId().asDriveFile().delete(googleApiClient);
                }
            }
        });
    }

    private int createBackup(OutputStream outputStream) throws IOException {
        List<TrackingItem> items = trackingItemRepository.findAll();

        if (items != null && !items.isEmpty()) {
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream, "utf-8"), SEPARATOR, QUOTE_CHARACTER);
            CsvTransformer csvTransformer = new CsvTransformer();

            for (TrackingItem item : items) {
                csvWriter.writeNext(csvTransformer.toStringArray(item));
            }

            csvWriter.flush();
            csvWriter.close();
        }

        return items == null ? 0 : items.size();
    }

    private void importBackup(InputStream inputStream) throws IOException {
        CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream), SEPARATOR, QUOTE_CHARACTER);
        CsvTransformer csvTransformer = new CsvTransformer();

        for (String[] line : csvReader.readAll()) {
            TrackingItem trackingItem = csvTransformer.fromArrayString(line);
            LOGGER.debug("Import item -> {}", trackingItem);

            // TODO persist
        }
    }

    private void showMessage(String message) {
        Snackbar.make(
                activity.getWindow().findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_SHORT).show();
    }
}
