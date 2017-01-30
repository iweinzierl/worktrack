package de.iweinzierl.worktrack.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.LocalDate;

import java.util.List;

import de.iweinzierl.worktrack.R;

public class WeekSpinnerAdapter extends BaseAdapter {

    public static class Week {
        private final int year;
        private final int week;

        public Week(int year, int week) {
            this.year = year;
            this.week = week;
        }

        public int getYear() {
            return year;
        }

        public int getWeek() {
            return week;
        }
    }

    private final Context context;
    private final List<Week> items;

    public WeekSpinnerAdapter(Context context, List<Week> items) {
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

        Week week = (Week) getItem(position);
        LocalDate date = LocalDate.now().withYear(week.getYear()).withWeekOfWeekyear(week.getWeek());

        View dropDownView = inflater.inflate(R.layout.spinner_week_dropdown, parent, false);
        UiUtils.setSafeText(dropDownView, R.id.weekNum, String.valueOf(week.getWeek()));
        UiUtils.setSafeText(dropDownView, R.id.startDateView, date.dayOfWeek().withMinimumValue().toString("dd.MM"));
        UiUtils.setSafeText(dropDownView, R.id.endDateView, date.dayOfWeek().withMaximumValue().toString("dd.MM"));

        return dropDownView;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);

        Week week = (Week) getItem(i);
        LocalDate date = LocalDate.now().withYear(week.getYear()).withWeekOfWeekyear(week.getWeek());

        View itemView = inflater.inflate(R.layout.spinner_week, viewGroup, false);
        UiUtils.setSafeText(itemView, R.id.weekNum, String.valueOf(week.getWeek()));
        UiUtils.setSafeText(itemView, R.id.startDateView, date.dayOfWeek().withMinimumValue().toString("dd.MM"));
        UiUtils.setSafeText(itemView, R.id.endDateView, date.dayOfWeek().withMaximumValue().toString("dd.MM"));

        return itemView;
    }
}
