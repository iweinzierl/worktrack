package de.iweinzierl.worktrack;

import android.support.v4.view.ViewPager;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDate;

import java.util.Collections;
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
    }

    @SuppressWarnings("unchecked")
    private List<Week> calculateWeeks() {
        LocalDate now = LocalDate.now();
        LocalDate current = trackingItemRepository.findFirstLocalDate();

        if (current == null) {
            return Collections.EMPTY_LIST;
        }

        List<Week> weeks = Lists.newArrayList();
        Week.Builder builder = Week.newBuilder()
                .withYear(0)
                .withWeekNum(0);

        do {
            if (builder.getYear() != current.getYear() || builder.getWeekNum() != current.getWeekOfWeekyear()) {
                if (trackingItemRepository.hasItemsAt(current)) {
                    builder = Week.newBuilder()
                            .withYear(current.getYear())
                            .withWeekNum(current.getWeekOfWeekyear());

                    weeks.add(builder.build());
                }
            }

            current = current.plusDays(1);
        }
        while (!current.isAfter(now));

        return weeks;
    }
}
