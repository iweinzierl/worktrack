package de.iweinzierl.worktrack;

import android.support.v4.app.Fragment;
import android.widget.Spinner;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.util.List;

import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.view.adapter.WeekSpinnerAdapter;
import de.iweinzierl.worktrack.view.adapter.YearSpinnerAdapter;

@EFragment(R.layout.fragment_overview_chart)
public class OverviewChartActivityFragment extends Fragment {

    @ViewById
    BarChart barChart;

    @ViewById
    Spinner yearSpinner;

    @ViewById
    Spinner weekSpinner;

    @ColorRes(R.color.barChartOverHours)
    int colorOverHours;

    @ColorRes(R.color.barChartNormalHours)
    int colorNormalHours;

    public OverviewChartActivityFragment() {
    }

    @AfterViews
    protected void setup() {
        yearSpinner.setAdapter(new YearSpinnerAdapter(
                getContext(),
                Lists.newArrayList(2016, 2017)));

        weekSpinner.setAdapter(new WeekSpinnerAdapter(
                getContext(),
                Lists.newArrayList(
                        new WeekSpinnerAdapter.Week(2017, 1),
                        new WeekSpinnerAdapter.Week(2017, 2),
                        new WeekSpinnerAdapter.Week(2017, 3),
                        new WeekSpinnerAdapter.Week(2017, 4),
                        new WeekSpinnerAdapter.Week(2017, 5)
                )));
    }

    @UiThread
    public void setWeekDays(List<WeekDay> weekDays) {
        for (WeekDay day : weekDays) {
            float hours = (float) ((int) (day.getWorkingTime().getMillis() / (10 * 60 * 60))) / 100;
            barChart.addBar(new BarModel(
                    String.valueOf(day.getDate().toString("EEE")),
                    hours,
                    hours > 8 ? colorOverHours : colorNormalHours));
        }

        barChart.startAnimation();
    }
}
