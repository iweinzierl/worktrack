package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.TrackingItem;

public class TrackingItemAdapter extends RecyclerView.Adapter<TrackingItemViewHolder> implements ItemToucheHelperAdapter<TrackingItem> {

    private ActionCallback<TrackingItem> actionCallback;
    private Context context;

    private List<TrackingItem> items;

    @SuppressWarnings("unchecked")
    public TrackingItemAdapter(ActionCallback<TrackingItem> actionCallback) {
        this.actionCallback = actionCallback;
        this.items = Collections.EMPTY_LIST;
    }

    @Override
    public TrackingItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tracking_item, parent, false);
        return new TrackingItemViewHolder(context, view, actionCallback);
    }

    @Override
    public void onBindViewHolder(TrackingItemViewHolder holder, int position) {
        holder.apply(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onItemDismiss(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void onItemDismissRevert(int position) {
        notifyItemChanged(position);
    }

    @Override
    public TrackingItem getItem(int position) {
        return items.get(position);
    }

    public void setItems(List<TrackingItem> items) {
        this.items = Lists.newArrayList(items);
        notifyDataSetChanged();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void removeItem(TrackingItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }
}
