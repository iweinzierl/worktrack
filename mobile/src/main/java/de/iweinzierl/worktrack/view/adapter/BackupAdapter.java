package de.iweinzierl.worktrack.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.model.Backup;

public class BackupAdapter extends RecyclerView.Adapter<BackupViewHolder> {

    private List<Backup> items;

    @SuppressWarnings("unchecked")
    public BackupAdapter() {
        this.items = Collections.EMPTY_LIST;
    }

    @Override
    public BackupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_backup, parent, false);
        return new BackupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BackupViewHolder holder, int position) {
        Backup backup = items.get(position);
        holder.apply(backup);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Backup> items) {
        this.items = Lists.newArrayList(items);
        notifyDataSetChanged();
    }
}
