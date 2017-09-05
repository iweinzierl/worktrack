package de.iweinzierl.worktrack.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.Workplace;

public class WorkplaceAdapter extends RecyclerView.Adapter<WorkplaceViewHolder> {

    private List<Workplace> workplaces;

    @SuppressWarnings("unchecked")
    public WorkplaceAdapter() {
        this.workplaces = Collections.EMPTY_LIST;
    }

    @Override
    public WorkplaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_workplace, parent, false);
        return new WorkplaceViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(WorkplaceViewHolder holder, int position) {
        holder.apply(workplaces.get(position));
    }

    @Override
    public int getItemCount() {
        return workplaces.size();
    }

    public void setWorkplaces(List<Workplace> workplaces) {
        this.workplaces = Lists.newArrayList();
        this.workplaces.addAll(workplaces);
        this.notifyDataSetChanged();
    }
}
