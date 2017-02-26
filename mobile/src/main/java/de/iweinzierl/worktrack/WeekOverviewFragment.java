package de.iweinzierl.worktrack;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.SettingsHelper;

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

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    BarChart barChart;

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

        barChart.clearChart();

        for (WeekDay day : weekDays) {
            long millis = day.getWorkingTime().getMillis();
            duration = duration.plus(millis);

            float hours = (float) ((int) (millis / (10 * 60 * 60))) / 100;
            barChart.addBar(new BarModel(
                    String.valueOf(day.getDate().toString("EEE")),
                    hours,
                    hours > dailyWorkingHours ? barColorOverHours : colorNormalHours));

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

        barChart.startAnimation();
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
