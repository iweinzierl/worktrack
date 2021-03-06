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
import de.iweinzierl.worktrack.persistence.repository.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.repository.TrackingItemRepository;
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

    List<Week> weeks;

    LineChartData lineChartData;
    Line chartLine;

    public YearOverviewFragment() {
    }

    @AfterViews
    public void initChart() {
        chartLine = createChartLine();

        lineChartData = new LineChartData(Lists.newArrayList(chartLine));
        lineChartData.setAxisYLeft(createYAxis());
        lineChartData.setBaseValue(0);

        lineChart.setLineChartData(lineChartData);
        lineChart.setInteractive(false);
    }

    private void updateXAxis() {
        List<AxisValue> values = Lists.newArrayList();

        for (int i = weeks.get(0).getWeekNum(); i <= weeks.get(weeks.size() - 1).getWeekNum(); i++) {
            values.add(new AxisValue(i));
        }

        Axis axis = new Axis(values)
                .setHasTiltedLabels(false)
                .setHasLines(true)
                .setHasSeparationLine(true)
                .setName(xAxisLabel);

        lineChartData.setAxisXBottom(axis);
        lineChart.setLineChartData(lineChartData);
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
        this.weeks = stripEmptyWeeks(weeks);

        yearView.setText(String.valueOf(getConfiguredYear()));

        final int weeklyWorkingHours = new SettingsHelper(getContext()).getWeeklyWorkingHours();
        int overHours = 0;

        List<PointValue> values = new ArrayList<>();

        for (Week week : this.weeks) {
            long hours = week.getWorkingTime().getStandardHours();

            if (hours > 0) {
                overHours += hours - weeklyWorkingHours;
            }

            values.add(new PointValue(week.getWeekNum(), hours));
        }

        chartLine.setValues(values);
        lineChart.setLineChartData(lineChartData);
        overHoursView.setText(String.valueOf(overHours));

        updateXAxis();
    }

    private List<Week> stripEmptyWeeks(List<Week> weeks) {
        List<Week> revisedWeeks = Lists.newArrayList(weeks);
        for (int idx = revisedWeeks.size() - 1; idx >= 0; idx--) {
            if (revisedWeeks.get(idx).getWorkingTime().getMillis() == 0) {
                revisedWeeks.remove(idx);
            } else {
                break;
            }
        }

        List<Week> stripped = Lists.newArrayList();
        for (int idx = 0; idx < revisedWeeks.size(); idx++) {
            if (!stripped.isEmpty() || revisedWeeks.get(idx).getWorkingTime().getMillis() > 0) {
                stripped.add(revisedWeeks.get(idx));
            }
        }

        return stripped;
    }

    private Line createChartLine() {
        return new Line()
                .setColor(colorNormalHours)
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(false)
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
}
