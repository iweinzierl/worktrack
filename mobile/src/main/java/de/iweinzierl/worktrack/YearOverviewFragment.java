package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.SettingsHelper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

@EFragment(R.layout.fragment_year_overview)
public class YearOverviewFragment extends Fragment {

    public static final String ARGS_YEAR = "weekoverviewfragment.args.year";

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    LineChartView lineChart;

    @ViewById
    TextView yearView;

    @ViewById
    TextView overHoursView;

    @ColorRes(R.color.barChartNormalHours)
    int colorNormalHours;

    @StringRes(R.string.activity_yearoverview_chart_yaxis_label)
    String yAxisLabel;

    @StringRes(R.string.activity_yearoverview_chart_xaxis_label)
    String xAxisLabel;

    LineChartData lineChartData;
    Line chartLine;

    public YearOverviewFragment() {
    }

    @AfterViews
    public void initChart() {
        chartLine = createChartLine();

        lineChartData = new LineChartData(Lists.newArrayList(chartLine));
        lineChartData.setAxisXBottom(createXAxis());
        lineChartData.setAxisYLeft(createYAxis());
        lineChartData.setBaseValue(0);

        lineChart.setLineChartData(lineChartData);
        lineChart.setInteractive(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public int getConfiguredYear() {
        Bundle args = getArguments();
        return args == null
                ? -1
                : args.getInt(ARGS_YEAR);
    }

    public Year getYear() {
        return getConfiguredYear() == -1
                ? null
                : trackingItemRepository.findYear(getConfiguredYear());
    }

    @Background
    protected void updateUI() {
        Year year = getYear();
        if (year != null) {
            setWeeks(year.getWeeks());
        }
    }

    @UiThread
    public void setWeeks(List<Week> weeks) {
        yearView.setText(String.valueOf(getConfiguredYear()));

        final int weeklyWorkingHours = new SettingsHelper(getContext()).getWeeklyWorkingHours();
        int overHours = 0;

        List<Week> revisedWeeks = stripEmptyWeeks(weeks);
        List<PointValue> values = new ArrayList<>();

        for (Week week : revisedWeeks) {
            long hours = week.getWorkingTime().getStandardHours();

            if (hours > 0) {
                overHours += hours - weeklyWorkingHours;
            }

            values.add(new PointValue(week.getWeekNum(), hours));
        }

        chartLine.setValues(values);
        lineChart.setLineChartData(lineChartData);
        overHoursView.setText(String.valueOf(overHours));
    }

    private List<Week> stripEmptyWeeks(List<Week> weeks) {
        List<Week> revisedWeeks = Lists.newArrayList(weeks);

        for (int idx = revisedWeeks.size() - 1; idx >= 0; idx--) {
            if (revisedWeeks.get(idx).getWorkingTime().getMillis() == 0) {
                revisedWeeks.remove(idx);
            }
            else {
                break;
            }
        }

        for (int idx = 0; idx < revisedWeeks.size(); idx++) {
            if (revisedWeeks.get(idx).getWorkingTime().getMillis() == 0) {
                revisedWeeks.remove(idx);
            }
            else {
                break;
            }
        }

        return revisedWeeks;
    }

    private Line createChartLine() {
        return new Line()
                .setColor(colorNormalHours)
                .setHasLabels(false)
                .setHasLabelsOnlyForSelected(true)
                .setHasLines(true)
                .setHasPoints(true);
    }

    private Axis createYAxis() {
        List<AxisValue> yAxisValues = Lists.newArrayList(
                new AxisValue(0),
                new AxisValue(10),
                new AxisValue(20),
                new AxisValue(30),
                new AxisValue(40),
                new AxisValue(50),
                new AxisValue(60)
        );

        Axis axis = new Axis(yAxisValues);
        axis.setHasSeparationLine(true);
        axis.setHasLines(true);
        axis.setName(yAxisLabel);

        return axis;
    }

    private Axis createXAxis() {
        List<AxisValue> values = Lists.newArrayList();

        for (int i = 1; i <= 52; i++) {
            values.add(new AxisValue(i));
        }

        Axis axis = new Axis(values);
        axis.setHasTiltedLabels(false);
        axis.setHasLines(true);
        axis.setHasSeparationLine(true);
        axis.setName(xAxisLabel);

        return axis;
    }
}
