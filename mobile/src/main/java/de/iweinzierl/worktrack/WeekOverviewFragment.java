package de.iweinzierl.worktrack;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.iweinzierl.worktrack.model.WeekDay;

@EFragment(R.layout.fragment_week_overview)
public class WeekOverviewFragment extends Fragment {

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix(" hours ")
            .appendMinutes()
            .appendSuffix(" min ")
            .toFormatter();

    @ViewById
    BarChart barChart;

    @ColorRes(R.color.barChartOverHours)
    int colorOverHours;

    @ColorRes(R.color.barChartNormalHours)
    int colorNormalHours;

    @ViewById
    TextView weekView;

    @ViewById
    TextView yearView;

    @ViewById
    TextView durationView;

    public WeekOverviewFragment() {
    }

    @AfterViews
    protected void setup() {
    }

    @UiThread
    public void setWeekDays(List<WeekDay> weekDays) {
        Duration duration = new Duration(0);
        LocalDate date = null;

        for (WeekDay day : weekDays) {
            long millis = day.getWorkingTime().getMillis();
            duration = duration.plus(millis);

            float hours = (float) ((int) (millis / (10 * 60 * 60))) / 100;
            barChart.addBar(new BarModel(
                    String.valueOf(day.getDate().toString("EEE")),
                    hours,
                    hours > 8 ? colorOverHours : colorNormalHours));

            if (!day.getItems().isEmpty()) {
                date = day.getItems().get(0).getEventTime().toLocalDate();
            }
        }

        if (date != null) {
            weekView.setText(String.valueOf(date.getWeekOfWeekyear()));
            yearView.setText(String.valueOf(date.getYear()));
            durationView.setText(periodFormatter.print(duration.toPeriod()));
        }

        barChart.startAnimation();
    }
}
