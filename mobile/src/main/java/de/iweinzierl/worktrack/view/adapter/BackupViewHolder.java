package de.iweinzierl.worktrack.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.text.NumberFormat;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.model.BackupMetaData;

class BackupViewHolder extends RecyclerView.ViewHolder {

    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

    static {
        DECIMAL_FORMAT.setMinimumFractionDigits(2);
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    private final TextView titleView;
    private final TextView lastModifiedView;
    private final TextView sizeView;
    private final TextView itemCountView;

    BackupViewHolder(View itemView) {
        super(itemView);

        this.titleView = UiUtils.getGeneric(TextView.class, itemView, R.id.title);
        this.lastModifiedView = UiUtils.getGeneric(TextView.class, itemView, R.id.last_modified);
        this.sizeView = UiUtils.getGeneric(TextView.class, itemView, R.id.size);
        this.itemCountView = UiUtils.getGeneric(TextView.class, itemView, R.id.item_count);
    }

    void apply(BackupMetaData backupMetaData) {
        titleView.setText(backupMetaData.getTitle());
        lastModifiedView.setText(formatLastModified(backupMetaData.getLastModified()));
        sizeView.setText(formatSize(backupMetaData.getSize()));
        itemCountView.setText(String.valueOf(backupMetaData.getItemCount()));
    }

    private String formatLastModified(LocalDateTime lastModified) {
        LocalDateTime now = LocalDateTime.now();
        Period period = new Period(lastModified, now);

        return period.getDays() == 0 ? period.getHours() + " h" : period.getDays() + " d";
    }

    private String formatSize(long bytes) {
        double kiloBytes = ((double) bytes) / 1024;
        return DECIMAL_FORMAT.format(kiloBytes) + " kB";
    }
}
