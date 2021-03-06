package de.iweinzierl.worktrack.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.comparator.BackupLastModifiedComparator;

public class BackupAdapter extends RecyclerView.Adapter<BackupViewHolder> implements ItemToucheHelperAdapter<BackupMetaData> {


    private ActionCallback<BackupMetaData> actionCallback;
    private List<BackupMetaData> items;

    @SuppressWarnings("unchecked")
    private BackupAdapter() {
        this.items = Collections.EMPTY_LIST;
    }

    public BackupAdapter(ActionCallback<BackupMetaData> actionCallback) {
        this();
        this.actionCallback = actionCallback;
    }

    @Override
    public BackupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_backup, parent, false);
        return new BackupViewHolder(view, actionCallback);
    }

    @Override
    public void onBindViewHolder(final BackupViewHolder holder, final int position) {
        final BackupMetaData backupMetaData = items.get(position);
        holder.apply(backupMetaData);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<BackupMetaData> items) {
        this.items = Lists.newArrayList(items);
        Collections.sort(this.items, new BackupLastModifiedComparator());
        notifyDataSetChanged();
    }

    public void addItem(BackupMetaData backupMetaData) {
        this.items.add(backupMetaData);
        Collections.sort(this.items, new BackupLastModifiedComparator());
        notifyDataSetChanged();
    }

    public void removeItem(BackupMetaData backupMetaData) {
        this.items.remove(backupMetaData);
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemDismissRevert(int position) {
        notifyItemChanged(position);
    }

    @Override
    public BackupMetaData getItem(int position) {
        return items.get(position);
    }
}
