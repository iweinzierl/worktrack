package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.TrackingItem;

public class TrackingItemAdapter extends RecyclerView.Adapter<TrackingItemViewHolder> {

    private final Context context;
    private final List<TrackingItem> items;

    public TrackingItemAdapter(@NonNull Context context, @NonNull List<TrackingItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public TrackingItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_trackingitem, parent, false);
        return new TrackingItemViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(TrackingItemViewHolder holder, int position) {
        holder.apply(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
