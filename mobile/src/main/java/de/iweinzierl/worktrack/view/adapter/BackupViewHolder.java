package de.iweinzierl.worktrack.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.model.Backup;

class BackupViewHolder extends RecyclerView.ViewHolder {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final TextView lastModifiedView;
    private final TextView sizeView;

    BackupViewHolder(View itemView) {
        super(itemView);

        this.lastModifiedView = UiUtils.getGeneric(TextView.class, itemView, R.id.last_modified);
        this.sizeView = UiUtils.getGeneric(TextView.class, itemView, R.id.size);
    }

    void apply(Backup backup) {
        lastModifiedView.setText(backup.getLastModified().toString(DATETIME_PATTERN));
        sizeView.setText(formatSize(backup.getSize()));
    }

    private String formatSize(long bytes) {
        double kiloBytes = ((double) bytes) / 1024;
        double normalized = ((int) (kiloBytes * 1000)) / 1000d;

        return normalized + " kB";
    }
}
