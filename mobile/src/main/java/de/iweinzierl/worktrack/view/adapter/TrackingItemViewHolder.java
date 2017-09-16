package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

class TrackingItemViewHolder extends RecyclerView.ViewHolder {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm";

    private final ActionCallback<TrackingItem> actionCallback;
    private final Context context;

    private final View typeIndicator;
    private final TextView eventDateView;
    private final TextView eventTimeView;
    private final TextView eventLocationView;
    private final TextView workplaceNameView;
    private final TextView typeView;
    private final View discardButton;

    TrackingItemViewHolder(Context context, View itemView, ActionCallback<TrackingItem> actionCallback) {
        super(itemView);

        this.context = context;
        this.actionCallback = actionCallback;

        typeIndicator = itemView.findViewById(R.id.typeIndicator);
        eventDateView = (TextView) itemView.findViewById(R.id.eventDate);
        eventTimeView = (TextView) itemView.findViewById(R.id.eventTime);
        eventLocationView = (TextView) itemView.findViewById(R.id.eventLocation);
        workplaceNameView = (TextView) itemView.findViewById(R.id.workplaceName);
        typeView = (TextView) itemView.findViewById(R.id.type);
        discardButton = itemView.findViewById(R.id.discard);
    }

    void apply(final TrackingItem item) {
        Resources.Theme theme = context.getTheme();

        eventDateView.setText(item.getEventTime().toString(DATE_PATTERN));
        eventTimeView.setText(item.getEventTime().toString(TIME_PATTERN));
        typeView.setText(item.getType().name());

        if (item.getTriggerEventLat() != 0 && item.getTriggerEventLon() != 0) {
            eventLocationView.setText(context.getString(R.string.location_template,
                    item.getTriggerEventLat(), item.getTriggerEventLon()));
            workplaceNameView.setText(item.getWorkplaceName());
        }

        if (item.getType() == TrackingItemType.CHECKIN) {
            typeIndicator.setBackgroundColor(context.getResources().getColor(R.color.colorCheckIn, theme));
        } else {
            typeIndicator.setBackgroundColor(context.getResources().getColor(R.color.colorCheckOut, theme));
        }

        if (item.getCreationType() == CreationType.AUTO) {
            typeView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_auto_added_black_24px, 0, 0, 0);
        } else {
            typeView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_manual_added_24dp, 0, 0, 0);
        }

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCallback.onDeleteItem(item);
            }
        });
    }
}
