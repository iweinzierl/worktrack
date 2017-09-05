package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

class TrackingItemViewHolder extends RecyclerView.ViewHolder {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final Context context;

    private final ImageView iconView;
    private final ImageView manualIconView;
    private final TextView eventTimeView;
    private final TextView typeView;

    TrackingItemViewHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;

        iconView = (ImageView) itemView.findViewById(R.id.iconView);
        manualIconView = (ImageView) itemView.findViewById(R.id.manualIconView);
        eventTimeView = (TextView) itemView.findViewById(R.id.eventtime);
        typeView = (TextView) itemView.findViewById(R.id.type);
    }

    void apply(TrackingItem item) {
        Drawable drawable = item.getType() == TrackingItemType.CHECKIN
                ? context.getDrawable(R.drawable.ic_arrow_forward_green_32px)
                : context.getDrawable(R.drawable.ic_arrow_backward_red_32px);

        iconView.setImageDrawable(drawable);
        eventTimeView.setText(item.getEventTime().toString(DATETIME_PATTERN));
        typeView.setText(item.getType().name());

        if (item.getCreationType() == CreationType.MANUAL) {
            manualIconView.setImageDrawable(context.getDrawable(R.drawable.ic_person_add_black_32px));
        }
    }
}
