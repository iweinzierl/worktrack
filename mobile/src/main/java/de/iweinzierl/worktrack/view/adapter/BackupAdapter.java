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

    public interface ClickCallback {

        void onClick(int position, Backup backup);
    }

    private List<Backup> items;
    private ClickCallback clickCallback;

    @SuppressWarnings("unchecked")
    public BackupAdapter() {
        this.items = Collections.EMPTY_LIST;
    }

    public BackupAdapter(ClickCallback clickCallback) {
        this();
        this.clickCallback = clickCallback;
    }

    @Override
    public BackupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_backup, parent, false);
        return new BackupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BackupViewHolder holder, final int position) {
        final Backup backup = items.get(position);
        holder.apply(backup);

        holder.itemView.findViewById(R.id.itemRoot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCallback != null) {
                    clickCallback.onClick(position, backup);
                }
            }
        });
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
