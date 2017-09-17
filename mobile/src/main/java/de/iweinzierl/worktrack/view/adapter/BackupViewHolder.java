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

    static {
        DECIMAL_FORMAT.setMinimumFractionDigits(2);
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    private final ActionCallback<BackupMetaData> actionCallback;

    private final TextView titleView;
    private final TextView lastModifiedView;
    private final TextView sizeView;
    private final TextView itemsView;
    private final TextView workplacesView;
    private final View restoreButton;
    private final View discardButton;

    BackupViewHolder(View itemView, ActionCallback<BackupMetaData> actionCallback) {
        super(itemView);
        this.actionCallback = actionCallback;

        this.titleView = UiUtils.getGeneric(TextView.class, itemView, R.id.title);
        this.lastModifiedView = UiUtils.getGeneric(TextView.class, itemView, R.id.lastModified);
        this.sizeView = UiUtils.getGeneric(TextView.class, itemView, R.id.size);
        this.itemsView = UiUtils.getGeneric(TextView.class, itemView, R.id.items);
        this.workplacesView = UiUtils.getGeneric(TextView.class, itemView, R.id.workplaces);
        this.restoreButton = UiUtils.getView(itemView, R.id.restore);
        this.discardButton = UiUtils.getView(itemView, R.id.discard);
    }

    void apply(final BackupMetaData backupMetaData) {
        titleView.setText(backupMetaData.getTitle());
        lastModifiedView.setText(formatLastModified(backupMetaData.getLastModified()));
        sizeView.setText(formatSize(backupMetaData.getSize()));
        itemsView.setText(String.valueOf(backupMetaData.getItemCount()));
        workplacesView.setText(String.valueOf(backupMetaData.getWorkplaceCount()));

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCallback.onSelectItem(backupMetaData);
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCallback.onDeleteItem(backupMetaData);
            }
        });
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
