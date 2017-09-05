package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.Workplace;

public class WorkplaceViewHolder extends RecyclerView.ViewHolder {

    private final Context context;

    private final TextView titleView;

    public WorkplaceViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;

        titleView = (TextView) itemView.findViewById(R.id.title);
    }

    void apply(Workplace workplace) {
        titleView.setText(workplace.getName());
    }
}
