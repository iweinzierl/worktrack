package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.iweinzierl.android.utils.UiUtils;

import de.iweinzierl.worktrack.R;

public class WeekSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final int minWeek;
    private final int maxWeek;

    public WeekSpinnerAdapter(Context context, int minWeek, int maxWeek) {
        super();
        this.context = context;
        this.minWeek = minWeek;
        this.maxWeek = maxWeek;
    }

    @Override
    public int getCount() {
        return maxWeek - minWeek + 1;
    }

    @Override
    public Object getItem(int i) {
        return minWeek + i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        int week = (int) getItem(position);

        View dropDownView = inflater.inflate(R.layout.spinner_week_dropdown, parent, false);
        UiUtils.setSafeText(dropDownView, R.id.weekNum, String.valueOf(week));

        return dropDownView;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);

        int week = (int) getItem(position);

        View itemView = inflater.inflate(R.layout.spinner_week, viewGroup, false);
        UiUtils.setSafeText(itemView, R.id.weekNum, String.valueOf(week));

        return itemView;
    }
}
