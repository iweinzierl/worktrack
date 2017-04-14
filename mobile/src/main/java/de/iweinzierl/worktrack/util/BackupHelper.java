package de.iweinzierl.worktrack.util;

import android.support.annotation.NonNull;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.common.base.Joiner;

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

    public interface BackupCallback {
        void onCreationSuccessful();

        void onCreationFailed();

        void onImportSuccessful();

        void onImportFailed();
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupHelper.class.getName());

    private static final String MIME_TYPE = "text/plain";
    private static final String BACKUP_ENDING = ".csv";
    private static final char SEPARATOR = ',';
    private static final char QUOTE_CHARACTER = '"';

    private final BackupCallback callback;
    private final TrackingItemRepository trackingItemRepository;
    private final GoogleApiClient googleApiClient;

    private final ResultCallback<DriveFolder.DriveFileResult> creationCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
            if (callback != null && result.getStatus().isSuccess()) {
                callback.onCreationSuccessful();
            } else if (callback != null && !result.getStatus().isSuccess()) {
                callback.onCreationFailed();
            }
        }
    };

    private final DriveFile.DownloadProgressListener progressListener = new DriveFile.DownloadProgressListener() {
        @Override
        public void onProgress(long bytesDownloaded, long bytesExpected) {
            // TODO update progress bar
        }
    };

    public BackupHelper(BackupCallback callback, TrackingItemRepository trackingItemRepository, GoogleApiClient googleApiClient) {
        Objects.requireNonNull(trackingItemRepository, "trackingItemRepository must not be null");
        Objects.requireNonNull(googleApiClient, "googleApiClient must not be null");

        this.callback = callback;
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
                        if (callback != null && !result.getStatus().isSuccess()) {
                            callback.onCreationFailed();
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
                            LOGGER.error("BACKUP CREATION FAILED", e);
                            if (callback != null) {
                                callback.onCreationFailed();
                            }
                        }
                    }
                });
    }

    public void importBackup(@NonNull final String backupDriveId) {
        Objects.requireNonNull(backupDriveId, "backupDriveId must not be null");

        DriveId.decodeFromString(backupDriveId)
                .asDriveFile()
                .open(googleApiClient, DriveFile.MODE_READ_ONLY, progressListener)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        try {
                            importBackup(driveContentsResult.getDriveContents().getInputStream());
                        } catch (IOException e) {
                            LOGGER.error("BACKUP IMPORT FAILED", e);
                            if (callback != null) {
                                callback.onImportFailed();
                            }
                        }
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

    private void importBackup(final InputStream inputStream) throws IOException {
        trackingItemRepository.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                try {
                    final CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream), SEPARATOR, QUOTE_CHARACTER);
                    final CsvTransformer csvTransformer = new CsvTransformer();

                    for (String[] line : csvReader.readAll()) {
                        TrackingItem trackingItem = csvTransformer.fromArrayString(line);

                        if (trackingItem != null) {
                            trackingItem.setId(null);

                            trackingItemRepository.deleteByEventTime(trackingItem.getEventTime());
                            trackingItemRepository.save(trackingItem);

                            LOGGER.debug("Import item -> {}", trackingItem);
                        } else {
                            LOGGER.warn("Skipped line in backup: {}", Joiner.on(",").join(line));
                        }
                    }

                    List<TrackingItem> trackingItems = trackingItemRepository.findAll();
                    LOGGER.info("{} tracking items in database after import", trackingItems.size());

                    if (callback != null) {
                        callback.onImportSuccessful();
                    }
                } catch (Exception e) {
                    LOGGER.error("BACKUP IMPORT ERROR", e);

                    if (callback != null) {
                        callback.onImportFailed();
                    }
                }
            }
        });
    }
}
