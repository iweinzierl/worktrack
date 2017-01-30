package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.iweinzierl.android.utils.UiUtils;

import java.util.List;

import de.iweinzierl.worktrack.R;

public class YearSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> items;

    public YearSpinnerAdapter(Context context, List<Integer> items) {
        super();
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View dropDownView = inflater.inflate(R.layout.spinner_year_dropdown, parent, false);
        UiUtils.setSafeText(dropDownView, R.id.year, String.valueOf(getItem(position)));

        return dropDownView;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.spinner_year, viewGroup, false);
        UiUtils.setSafeText(itemView, R.id.year, String.valueOf(getItem(i)));

        return itemView;
    }
}
