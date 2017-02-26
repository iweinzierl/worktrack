package de.iweinzierl.worktrack;

import android.content.DialogInterface;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.common.collect.Lists;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.DaoSessionFactory;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;
import de.iweinzierl.worktrack.util.AsyncCallback;
import de.iweinzierl.worktrack.view.adapter.DayOverviewFragmentAdapter;

@EActivity
public class DayOverviewActivity extends BaseActivity implements DayOverviewFragment.TrackingItemCallback {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("DayOverviewActivity");

    @Bean
    DaoSessionFactory sessionFactory;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    ViewPager pager;

    DayOverviewFragmentAdapter pagerAdapter;

    @ViewById
    FloatingActionMenu actionMenu;

    @ViewById
    FloatingActionButton checkinAction;

    @ViewById
    FloatingActionButton checkoutAction;

    @Override
    int getLayoutId() {
        return R.layout.activity_day_overview;
    }

    @AfterViews
    protected void setupPager() {
        LocalDate first = trackingItemRepository.findFirstLocalDate();
        LocalDate last = LocalDate.now();

        pagerAdapter = new DayOverviewFragmentAdapter(
                getSupportFragmentManager(),
                createListOfDays(first, last));
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(pagerAdapter.getCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_demo_data:
                addDemoData();
                return true;
            case R.id.action_select_date:
                showDatePickerDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDeleteItem(TrackingItem item) {
        deleteTrackingItem(item, new AsyncCallback() {
            @Override
            public void callback() {
                updateUi();
                Snackbar.make(findViewById(android.R.id.content), "Deleted Event", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
    }

    @Click(R.id.checkinAction)
    protected void checkinManually() {
        closeActionMenu(false);
        saveTrackingItem(new TrackingItem(TrackingItemType.CHECKIN, DateTime.now(), CreationType.MANUAL), new AsyncCallback() {
            @Override
            public void callback() {
                updateUi();
                Snackbar.make(findViewById(android.R.id.content), "Added Event", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
    }

    @Click(R.id.checkinAtAction)
    protected void checkinAtManually() {
        closeActionMenu(false);
        showDateTimePickerDialog(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                saveTrackingItem(new TrackingItem(
                        TrackingItemType.CHECKIN,
                        new DateTime(date.getTime()),
                        CreationType.MANUAL
                ), new AsyncCallback() {
                    @Override
                    public void callback() {
                        updateUi();
                        Snackbar.make(findViewById(android.R.id.content), "Added Event", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNegativeButtonClick(Date date) {
            }
        });
    }

    @Click(R.id.checkoutAction)
    protected void checkoutManually() {
        closeActionMenu(false);
        saveTrackingItem(new TrackingItem(TrackingItemType.CHECKOUT, DateTime.now(), CreationType.MANUAL), new AsyncCallback() {
            @Override
            public void callback() {
                updateUi();
                Snackbar.make(findViewById(android.R.id.content), "Added Event", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
    }

    @Click(R.id.checkoutAtAction)
    protected void checkoutAtManually() {
        closeActionMenu(false);
        showDateTimePickerDialog(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                saveTrackingItem(new TrackingItem(
                        TrackingItemType.CHECKOUT,
                        new DateTime(date.getTime()),
                        CreationType.MANUAL
                ), new AsyncCallback() {
                    @Override
                    public void callback() {
                        updateUi();
                        Snackbar.make(findViewById(android.R.id.content), "Added Event", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNegativeButtonClick(Date date) {
            }
        });
    }

    @UiThread
    protected void closeActionMenu() {
        closeActionMenu(true);
    }

    @UiThread
    void closeActionMenu(boolean animate) {
        actionMenu.close(animate);
    }

    @Background
    protected void saveTrackingItem(TrackingItem item) {
        saveTrackingItem(item, null);
    }

    @Background
    protected void saveTrackingItem(TrackingItem item, AsyncCallback callback) {
        trackingItemRepository.save(item);

        if (callback != null) {
            callback.callback();
        }
    }

    @Background
    protected void deleteTrackingItem(TrackingItem item) {
        deleteTrackingItem(item, null);
    }

    @Background
    protected void deleteTrackingItem(TrackingItem item, AsyncCallback callback) {
        trackingItemRepository.delete(item);

        if (callback != null) {
            callback.callback();
        }
    }

    @UiThread
    protected void updateUi() {
        DayOverviewFragment fragment = (DayOverviewFragment) pagerAdapter.getItem(pager.getCurrentItem());
        fragment.updateUI();
    }

    @UiThread
    protected void showDateTimePickerDialog(SwitchDateTimeDialogFragment.OnButtonClickListener listener) {
        LocalDateTime now = LocalDateTime.now();

        SwitchDateTimeDialogFragment dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                "Title example",
                "OK",
                "Cancel"
        );

        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(now.getYear() - 5, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth(), 23, 59).getTime());
        dateTimeFragment.setDefaultDateTime(new GregorianCalendar(now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour()).getTime());

        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            LOGGER.error("Problem while displaying datetime picker dialog", e);
        }

        dateTimeFragment.setOnButtonClickListener(listener);
        dateTimeFragment.show(getSupportFragmentManager(), "dialog_time");
    }

    @UiThread
    protected void showDatePickerDialog() {
        LocalDate now = LocalDate.now();
        LocalDate min = trackingItemRepository.findFirstLocalDate();

        final DatePicker datePicker = new DatePicker(this);
        datePicker.setMaxDate(now.toDate().getTime());
        datePicker.setMinDate(min.toDate().getTime());

        new AlertDialog.Builder(this)
                .setView(datePicker)
                .setTitle(R.string.activity_dayoverview_action_select_date)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LocalDate selected = new LocalDate(
                                datePicker.getYear(),
                                datePicker.getMonth() + 1, // 0 based months
                                datePicker.getDayOfMonth()
                        );

                        if (selected.isAfter(LocalDate.now())) {
                            Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Cannot jump into the future",
                                    Snackbar.LENGTH_LONG).show();
                        } else {
                            navigateTo(selected);
                        }
                    }
                })
                .show();
    }

    @UiThread
    protected void navigateTo(LocalDate date) {
        int pos = pagerAdapter.findPosition(date);
        if (pos >= 0 && pos < pagerAdapter.getCount()) {
            pager.setCurrentItem(pos);
        }
    }

    @SuppressWarnings("unchecked")
    private List<LocalDate> createListOfDays(LocalDate first, LocalDate last) {
        if (first == null) {
            return Lists.newArrayList(LocalDate.now());
        }

        List<LocalDate> dates = Lists.newArrayList();
        LocalDate current = first;

        do {
            dates.add(current);
            current = current.plusDays(1);
        }
        while (!current.isAfter(last));

        return dates;
    }

    private void addDemoData() {
        new AlertDialog.Builder(this)
                .setTitle("Demo Data")
                .setMessage("Really delete existing data to add demo data?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eraseDataAndAddDemoData();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    protected void eraseDataAndAddDemoData() {
        trackingItemRepository.deleteAll();

        LocalDate mon = DateTime.now().toLocalDate().dayOfWeek().withMinimumValue();
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 7, 55), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 12, 10), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 13, 25), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 18, 20), CreationType.AUTO));

        LocalDate tue = mon.minusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 8, 30), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 12, 25), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 13, 5), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 18, 9), CreationType.AUTO));

        LocalDate wed = tue.minusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 8, 53), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 13, 10), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 13, 28), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 19, 5), CreationType.AUTO));

        LocalDate thu = wed.minusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 5, 50), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 12, 7), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 13, 5), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 20, 10), CreationType.AUTO));

        LocalDate fri = thu.minusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 9, 2), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 13, 7), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 14, 0), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 15, 38), CreationType.AUTO));

        Snackbar.make(findViewById(android.R.id.content), "Erased existing and added demo data", BaseTransientBottomBar.LENGTH_SHORT).show();

        setupPager();
        updateUi();
    }
}