package de.iweinzierl.worktrack;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;

@EActivity
public class WeekOverviewActivity extends BaseActivity {

    @FragmentById(R.id.fragment)
    WeekOverviewFragment fragment;

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

        fragment.setWeekDays(days);
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
}
