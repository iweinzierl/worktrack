package de.iweinzierl.worktrack;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.view.adapter.YearOverviewFragmentAdapter;

@EActivity
public class YearOverviewActivity extends BaseActivity {

    private static final int REQUEST_SEND_MAIL = 110;

    private static final String[] PERMISSIONS_EMAIL = new String[]{
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @ViewById
    ViewPager pager;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    YearOverviewFragmentAdapter yearOverviewFragmentAdapter;

    @Override
    int getLayoutId() {
        return R.layout.activity_week_overview;
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigateTo(LocalDate.now().getYear());
    }

    @AfterInject
    protected void setup() {
        yearOverviewFragmentAdapter = new YearOverviewFragmentAdapter(
                getSupportFragmentManager(), calculateYears());
    }

    @AfterViews
    protected void setupUI() {
        pager.setAdapter(yearOverviewFragmentAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_week_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_date:
                // TODO
                return true;
            case R.id.action_export_email:
                exportToMailRequest();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SEND_MAIL) {
            exportToMail();
        }
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

    @UiThread
    protected void navigateTo(int yearNum) {
        Year year = Year.newBuilder().withYear(yearNum).build();
        int pos = yearOverviewFragmentAdapter.findPosition(year);

        if (pos >= 0) {
            pager.setCurrentItem(pos);
        } else {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Unable to find: " + year, Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    private void exportToMailRequest() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_EMAIL, REQUEST_SEND_MAIL);
    }

    private void exportToMail() {
    }

    @SuppressWarnings("unchecked")
    private List<Year> calculateYears() {
        LocalDate now = LocalDate.now();
        LocalDate min = trackingItemRepository.findFirstLocalDate();

        if (min == null) {
            return Collections.EMPTY_LIST;
        }

        int currentYear = min.getYear();

        List<Year> years = Lists.newArrayList();

        do {
            years.add(trackingItemRepository.findYear(currentYear));
            currentYear++;
        }
        while (currentYear <= now.getYear());

        return years;
    }
}
