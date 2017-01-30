package de.iweinzierl.worktrack;

import android.support.v4.app.Fragment;
import android.widget.Spinner;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.util.List;

import de.iweinzierl.worktrack.view.adapter.YearSpinnerAdapter;
import de.iweinzierl.worktrack.view.adapter.WeekSpinnerAdapter;

@EFragment(R.layout.fragment_overview_chart)
public class OverviewChartActivityFragment extends Fragment {

    @ViewById
    BarChart barChart;

    @ViewById
    Spinner yearSpinner;

    @ViewById
    Spinner weekSpinner;

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

        barChart.addBarList(createModel());
        barChart.startAnimation();
    }

    private List<BarModel> createModel() {
        return Lists.newArrayList(
                new BarModel("Mon", 8.4f, 0xFF123456),
                new BarModel("Tue", 9.5f, 0xFF123456),
                new BarModel("Wed", 7.6f, 0xFF123456),
                new BarModel("Thu", 12.8f, 0xFF123456),
                new BarModel("Fri", 7.8f, 0xFF123456),
                new BarModel("Sat", 0f, 0xFF123456),
                new BarModel("Sun", 0f, 0xFF123456)
        );
    }
}
