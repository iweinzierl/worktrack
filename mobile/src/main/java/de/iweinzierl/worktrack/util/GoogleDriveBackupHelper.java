package de.iweinzierl.worktrack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Maps;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.iweinzierl.worktrack.model.Backup;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.util.gson.DateTimeConverter;
import de.iweinzierl.worktrack.view.settings.LastBackupPreferences;

public class GoogleDriveBackupHelper {

    public interface Callback<T> {
        void onSuccess(T result);
    }

    private static final String FILE_PROPERTY_DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String FILE_PROPERTY_ITEM_COUNT = "backup.item.count";
    private static final String FILE_PROPERTY_WORKPLACE_COUNT = "backup.workplace.count";
    private static final String FILE_PROPERTY_START_ITEM = "backup.item.start";
    private static final String FILE_PROPERTY_END_ITEM = "backup.item.end";

    private static final DateTimeFormatter DATE_TIME_FORMAT_GDRIVE = ISODateTimeFormat.dateTime();

    private static final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeConverter())
            .create();

    private final Context context;
    private final GoogleAccountCredential credentials;

    public GoogleDriveBackupHelper(Context context, GoogleAccountCredential credentials) {
        this.context = context;
        this.credentials = credentials;
    }

    public void listBackups(Callback<List<BackupMetaData>> callback) throws IOException {
        FileList appDataFolder = new Drive.Builder(transport, jsonFactory, credentials).build()
                .files()
                .list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name, modifiedTime, size, properties)")
                .setPageSize(25)
                .execute();

        List<BackupMetaData> backups = new ArrayList<>();

        for (File file : appDataFolder.getFiles()) {
            backups.add(createBackupMetaDataFromDriveFile(file));
        }

        callback.onSuccess(backups);
    }

    public void getBackup(String driveFileId, Callback<Backup> callback) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new Drive.Builder(transport, jsonFactory, credentials).build()
                .files()
                .get(driveFileId)
                .executeMediaAndDownloadTo(outputStream);

        String json = new String(outputStream.toByteArray(), "utf-8");
        Backup backup = GSON.fromJson(json, Backup.class);

        callback.onSuccess(backup);
    }

    public void createBackup(String title, List<TrackingItem> items, List<Workplace> workplaces, Callback<BackupMetaData> callback)
            throws IOException {
        Map<String, String> properties = createPropertiesMap(items, workplaces);

        File meta = new File();
        meta.setName(title);
        meta.setProperties(properties);
        meta.setParents(Collections.singletonList("appDataFolder"));

        Backup backup = createBackupData(title, items, workplaces);
        String tmpFilename = UUID.randomUUID().toString();
        String json = GSON.toJson(backup);
        java.io.File temp = FileUtil.toFile(context.getCacheDir(), tmpFilename, json);

        FileContent mediaContent = new FileContent("application/json", temp);

        Drive drive = new Drive.Builder(transport, jsonFactory, credentials).build();
        File file = drive.files().create(meta, mediaContent)
                .setFields("id, name, size")
                .execute();

        Long size = file.getSize();
        if (size != null && size > 0) {
            temp.delete();

            BackupMetaData metaData = new BackupMetaData.Builder()
                    .driveId(file.getId())
                    .itemCount(backup.getMetaData().getItemCount())
                    .workplaceCount(backup.getMetaData().getWorkplaceCount())
                    .lastModified(LocalDateTime.now())
                    .size(size)
                    .title(title)
                    .build();

            updateLastBackupPreferences(metaData);
            callback.onSuccess(metaData);
        }
    }

    public void deleteBackup(String driveFileId, Callback<String> callback) throws IOException {
        new Drive.Builder(transport, jsonFactory, credentials).build()
                .files()
                .delete(driveFileId)
                .execute();

        callback.onSuccess(driveFileId);
    }

    @NonNull
    private BackupMetaData createBackupMetaDataFromDriveFile(File file) {
        Map<String, String> properties = file.getProperties();
        int itemCount = !Strings.isNullOrEmpty(properties.get(FILE_PROPERTY_ITEM_COUNT))
                ? Integer.valueOf(properties.get(FILE_PROPERTY_ITEM_COUNT))
                : 0;

        int workplaceCount = !Strings.isNullOrEmpty(properties.get(FILE_PROPERTY_WORKPLACE_COUNT))
                ? Integer.valueOf(properties.get(FILE_PROPERTY_WORKPLACE_COUNT))
                : 0;

        LocalDateTime lastModified = LocalDateTime.parse(
                file.getModifiedTime().toStringRfc3339(),
                DATE_TIME_FORMAT_GDRIVE
        );

        return new BackupMetaData.Builder()
                .driveId(file.getId())
                .title(file.getName())
                .size(file.getSize())
                .lastModified(lastModified)
                .itemCount(itemCount)
                .workplaceCount(workplaceCount)
                .build();
    }

    private Backup createBackupData(String title, List<TrackingItem> items, List<Workplace> workplaces) {
        BackupMetaData metaData = new BackupMetaData.Builder()
                .itemCount(items.size())
                .workplaceCount(workplaces.size())
                .title(title)
                .build();

        return new Backup(metaData, items, workplaces);
    }

    private Map<String, String> createPropertiesMap(List<TrackingItem> items, List<Workplace> workplaces) {
        Map<String, String> properties = Maps.newHashMap();

        if (workplaces != null) {
            properties.put(FILE_PROPERTY_WORKPLACE_COUNT, String.valueOf(workplaces.size()));
        }

        if (items != null && !items.isEmpty()) {
            properties.put(FILE_PROPERTY_ITEM_COUNT, String.valueOf(items.size()));
            properties.put(FILE_PROPERTY_START_ITEM, items.get(0).getEventTime().toString(FILE_PROPERTY_DATETIME_FORMAT));
            properties.put(FILE_PROPERTY_END_ITEM, items.get(items.size() - 1).getEventTime().toString(FILE_PROPERTY_DATETIME_FORMAT));
        }

        return properties;
    }

    private void updateLastBackupPreferences(BackupMetaData metaData) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putLong(LastBackupPreferences.DATE.getProperty(), DateTime.now().getMillis())
                .putLong(LastBackupPreferences.SIZE.getProperty(), metaData.getSize())
                .putInt(LastBackupPreferences.ITEMS.getProperty(), metaData.getItemCount())
                .putInt(LastBackupPreferences.WORKPLACES.getProperty(), metaData.getWorkplaceCount())
                .apply();
    }
}
