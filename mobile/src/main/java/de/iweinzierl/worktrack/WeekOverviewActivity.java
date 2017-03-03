package de.iweinzierl.worktrack;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.MailHelper;
import de.iweinzierl.worktrack.view.adapter.WeekOverviewFragmentAdapter;
import de.iweinzierl.worktrack.view.dialog.WeekPickerDialogFragment;

@EActivity
public class WeekOverviewActivity extends BaseActivity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("WeekOverviewActivity");

    private static final int REQUEST_SEND_MAIL = 110;

    private static final String[] PERMISSIONS_EMAIL = new String[]{
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @ViewById
    ViewPager pager;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    WeekOverviewFragmentAdapter weekOverviewFragmentAdapter;

    @Override
    int getLayoutId() {
        return R.layout.activity_week_overview;
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshDate(LocalDate.now());
        navigateTo(
                LocalDate.now().getWeekOfWeekyear(),
                LocalDate.now().getYear()
        );
    }

    @AfterInject
    protected void setup() {
        weekOverviewFragmentAdapter = new WeekOverviewFragmentAdapter(
                getSupportFragmentManager(), calculateWeeks());
    }

    @AfterViews
    protected void setupUI() {
        pager.setAdapter(weekOverviewFragmentAdapter);
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
                showWeekPickerDialog();
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
    protected void navigateTo(int weekNum, int year) {
        Week week = Week.newBuilder().withWeekNum(weekNum).withYear(year).build();
        int pos = weekOverviewFragmentAdapter.findPosition(week);

        if (pos >= 0) {
            pager.setCurrentItem(pos);
        } else {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Unable to find: " + weekNum + "/" + year, Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    private void showWeekPickerDialog() {
        List<Week> weeks = calculateWeeks();
        int minYear = weeks == null || weeks.isEmpty() ? LocalDate.now().getYear() : weeks.get(0).getYear();
        int maxYear = weeks == null || weeks.isEmpty() ? LocalDate.now().getYear() : weeks.get(weeks.size() - 1).getYear();

        WeekPickerDialogFragment dialogFragment = WeekPickerDialogFragment.newInstance(
                1, minYear,
                52, maxYear
        );
        dialogFragment.setCallback(new WeekPickerDialogFragment.Callback() {
            @Override
            public void onWeekSelected(int week, int year) {
                navigateTo(week, year);
            }

            @Override
            public void onDismiss() {
            }
        });
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private void exportToMailRequest() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_EMAIL, REQUEST_SEND_MAIL);
    }

    private void exportToMail() {
        int currentItem = pager.getCurrentItem();
        Week meta = ((WeekOverviewFragment) weekOverviewFragmentAdapter.getItem(currentItem)).getWeek();

        try {
            Week week = trackingItemRepository.findWeek(meta.getYear(), meta.getWeekNum());
            MailHelper.sendMail(this, week);
        } catch (IOException e) {
            LOGGER.error("Export to mail failed", e);
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
