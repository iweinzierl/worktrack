package de.iweinzierl.worktrack;

import android.graphics.Typeface;
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
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.persistence.repository.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.repository.TrackingItemRepository;
import de.iweinzierl.worktrack.util.SettingsHelper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

@EFragment(R.layout.fragment_week_overview)
public class WeekOverviewFragment extends Fragment {

    public static final String ARGS_YEAR = "weekoverviewfragment.args.year";
    public static final String ARGS_WEEKNUM = "weekoverviewfragment.args.weeknum";

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix(" hours ")
            .appendMinutes()
            .appendSuffix(" min ")
            .toFormatter();

    @StringRes(R.string.activity_weekoverview_chart_xaxis_label)
    String xAxisLabel;

    @StringRes(R.string.activity_weekoverview_chart_yaxis_label)
    String yAxisLabel;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    ColumnChartView barChart;

    ColumnChartData columnChartData;

    @ColorRes(R.color.barChartOverHours)
    int barColorOverHours;

    @ColorRes(R.color.toolbarOverHours)
    int toolbarColorOverHours;

    @ColorRes(R.color.barChartNormalHours)
    int colorNormalHours;

    @ViewById
    TextView weekView;

    @ViewById
    TextView yearView;

    @ViewById
    TextView weekRangeView;

    @ViewById
    TextView durationView;

    public WeekOverviewFragment() {
    }

    @AfterViews
    protected void initChart() {
        Axis xAxis = new Axis();
        xAxis.setHasLines(true);
        xAxis.setHasSeparationLine(false);
        xAxis.setHasTiltedLabels(true);
        xAxis.setValues(Lists.newArrayList(
                new AxisValue(0).setLabel(getString(R.string.monday)),
                new AxisValue(1).setLabel(getString(R.string.tuesday)),
                new AxisValue(2).setLabel(getString(R.string.wednesday)),
                new AxisValue(3).setLabel(getString(R.string.thursday)),
                new AxisValue(4).setLabel(getString(R.string.friday)),
                new AxisValue(5).setLabel(getString(R.string.saturday)),
                new AxisValue(6).setLabel(getString(R.string.sunday))
        ));

        Axis yAxis = new Axis();
        yAxis.setHasLines(true);
        yAxis.setHasSeparationLine(false);
        yAxis.setHasTiltedLabels(true);

        columnChartData = new ColumnChartData();
        columnChartData.setAxisXBottom(xAxis);
        columnChartData.setAxisYLeft(yAxis);
        columnChartData.setStacked(false);

        barChart.setColumnChartData(columnChartData);
        barChart.setInteractive(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public Week getWeek() {
        Bundle args = getArguments();
        return args == null
                ? null
                : trackingItemRepository.findWeek(
                args.getInt(ARGS_YEAR),
                args.getInt(ARGS_WEEKNUM));
    }

    @Background
    protected void updateUI() {
        Week week = getWeek();
        if (week != null) {
            setWeekDays(week.getDays());
        }
    }

    @UiThread
    public void setWeekDays(List<WeekDay> weekDays) {
        final int dailyWorkingHours = new SettingsHelper(getActivity()).getDailyWorkingHours();
        final int weeklyWorkingHours = new SettingsHelper(getActivity()).getWeeklyWorkingHours();

        Duration duration = new Duration(0);
        LocalDate date = null;

        List<Column> columns = Lists.newArrayList();

        for (WeekDay day : weekDays) {
            long millis = day.getWorkingTime().getMillis();
            duration = duration.plus(millis);

            float hours = (float) ((int) (millis / (10 * 60 * 60))) / 100;
            SubcolumnValue subcolumnValue = new SubcolumnValue(hours);
            subcolumnValue.setLabel(String.valueOf(hours));
            subcolumnValue.setColor(
                    hours > dailyWorkingHours
                            ? barColorOverHours
                            : colorNormalHours
            );

            Column column = new Column(Lists.newArrayList(subcolumnValue));
            column.setHasLabels(true);
            columns.add(column);

            if (!day.getItems().isEmpty()) {
                date = day.getItems().get(0).getEventTime().toLocalDate();
            }
        }

        if (date != null) {
            weekView.setText(String.valueOf(date.getWeekOfWeekyear()));
            yearView.setText(String.valueOf(date.getYear()));
            weekRangeView.setText(createWeekRangeString(date));
            durationView.setText(periodFormatter.print(duration.toPeriod()));

            if (duration.isLongerThan(Hours.hours(weeklyWorkingHours).toStandardDuration())) {
                durationView.setTextColor(toolbarColorOverHours);
                durationView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        columnChartData.setColumns(columns);
        barChart.setColumnChartData(columnChartData);
    }

    private String createWeekRangeString(LocalDate dayInWeek) {
        LocalDate min = dayInWeek.withDayOfWeek(1);
        LocalDate max = dayInWeek.withDayOfWeek(7);

        return "(" +
                min.toString("dd.MM.") +
                " - " +
                max.toString("dd.MM.") +
                ")";
    }
}
