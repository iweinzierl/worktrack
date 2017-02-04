package de.iweinzierl.worktrack;

import android.support.v4.view.ViewPager;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.view.adapter.WeekOverviewFragmentAdapter;

@EActivity
public class WeekOverviewActivity extends BaseActivity {

    @ViewById
    ViewPager pager;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @Override
    int getLayoutId() {
        return R.layout.activity_week_overview;
    }

    @Override
    protected void onStart() {
        super.onStart();

        refreshDate(LocalDate.now());
    }

    @AfterViews
    protected void setupUI() {
        List<Week> weeks = calculateWeeks();
        pager.setAdapter(new WeekOverviewFragmentAdapter(getSupportFragmentManager(), weeks));
    }

    @Background
    protected void refreshDate(LocalDate date) {
        LocalDate start = date.dayOfWeek().withMinimumValue();
        List<WeekDay> days = Lists.newArrayList();

        for (int i = 0; i < 7; i++) {
            List<TrackingItem> items = trackingItemRepository.findByDate(start.plusDays(i));

            days.add(WeekDay.newBuilder()
                    .withLocalDate(start.plusDays(i))
                    .withItems(items)
                    .build());
        }

        displayWorkingHours(days);
    }

    @UiThread
    protected void displayWorkingHours(List<WeekDay> days) {
        Duration d = new Duration(0);
        for (WeekDay day : days) {
            d = d.plus(day.getWorkingTime());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(new PeriodFormatterBuilder()
                    .appendHours()
                    .appendSuffix(" hours ")
                    .appendMinutes()
                    .appendSuffix(" min ")
                    .toFormatter().print(new Period(d)));
        }
    }

    private List<Week> calculateWeeks() {
        // TODO
        return Lists.newArrayList(
                Week.newBuilder().withYear(2017).withWeekNum(1).build(),
                Week.newBuilder().withYear(2017).withWeekNum(2).build(),
                Week.newBuilder().withYear(2017).withWeekNum(3).build(),
                Week.newBuilder().withYear(2017).withWeekNum(4).build(),
                Week.newBuilder().withYear(2017).withWeekNum(5).build()
        );
    }
}
