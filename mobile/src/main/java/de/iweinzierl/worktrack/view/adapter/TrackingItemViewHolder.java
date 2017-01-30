package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

class TrackingItemViewHolder extends RecyclerView.ViewHolder {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final Context context;

    private final ImageView iconView;
    private final TextView eventTimeView;
    private final TextView typeView;

    TrackingItemViewHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;

        iconView = (ImageView) itemView.findViewById(R.id.iconView);
        eventTimeView = (TextView) itemView.findViewById(R.id.eventtime);
        typeView = (TextView) itemView.findViewById(R.id.type);
    }

    void apply(TrackingItem item) {
        SVG svg = item.getType() == TrackingItemType.CHECKIN
                ? SVGParser.getSVGFromResource(context.getResources(), R.raw.ic_arrow_forward_24px)
                : SVGParser.getSVGFromResource(context.getResources(), R.raw.ic_arrow_back_24px);

        iconView.setImageDrawable(svg.createPictureDrawable());
        iconView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        eventTimeView.setText(item.getEventTime().toString(DATETIME_PATTERN));
        typeView.setText(item.getType().name());
    }
}
