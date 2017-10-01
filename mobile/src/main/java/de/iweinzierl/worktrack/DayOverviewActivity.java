package de.iweinzierl.worktrack;

import android.content.DialogInterface;
import android.os.Bundle;
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

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;
import de.iweinzierl.worktrack.persistence.repository.exception.LimitApproachedException;
import de.iweinzierl.worktrack.persistence.repository.exception.PersistenceException;
import de.iweinzierl.worktrack.util.AsyncCallback;
import de.iweinzierl.worktrack.view.adapter.DayOverviewFragmentAdapter;
import de.iweinzierl.worktrack.view.dialog.OnlySupportedInProDialogFragment;

@EActivity
public class DayOverviewActivity extends BaseActivity implements DayOverviewFragment.TrackingItemCallback {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("DayOverviewActivity");

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
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int currentItem = pager.getCurrentItem();
                DayOverviewFragment fragment = (DayOverviewFragment) pagerAdapter.getItem(currentItem);
                updateTitle(fragment.getDate());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        DayOverviewFragment item = (DayOverviewFragment) pagerAdapter.getItem(pager.getCurrentItem());
        updateTitle(item.getDate());
    }

    private void updateTitle(LocalDate date) {
        if (date != null) {
            setTitle(date.toString(getString(R.string.util_date_format)));
        } else {
            setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                showMessage(getString(R.string.activity_dayoverview_message_event_deleted));
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
                showMessage(getString(R.string.activity_dayoverview_message_event_added));
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
                        showMessage(getString(R.string.activity_dayoverview_message_event_added));
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
                showMessage(getString(R.string.activity_dayoverview_message_event_added));
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
                        showMessage(getString(R.string.activity_dayoverview_message_event_added));
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
        try {
            trackingItemRepository.save(item);

            if (callback != null) {
                callback.callback();
            }
        } catch (LimitApproachedException e) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
            firebaseAnalytics.logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_FAILURE.name(), bundle);

            showLimitApproachedError();
        } catch (PersistenceException e) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
            firebaseAnalytics.logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_FAILURE.name(), bundle);

            showMessage(e.getMessage());
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
                getString(R.string.activity_dayoverview_action_select_datetime),
                getString(R.string.activity_dayoverview_action_select_date_confirm),
                getString(R.string.activity_dayoverview_action_select_date_cancel)
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

        if (min == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.activity_dayoverview_dialog_nodata_title)
                    .setMessage(R.string.activity_dayoverview_dialog_nodata_message)
                    .show();
        } else {
            final DatePicker datePicker = new DatePicker(this);
            datePicker.setMaxDate(now.toDate().getTime());
            datePicker.setMinDate(min.toDate().getTime());

            new AlertDialog.Builder(this)
                    .setView(datePicker)
                    .setTitle(R.string.activity_dayoverview_action_select_date)
                    .setPositiveButton(R.string.activity_dayoverview_action_select_date_confirm, new DialogInterface.OnClickListener() {
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
    }

    @UiThread
    protected void navigateTo(LocalDate date) {
        int pos = pagerAdapter.findPosition(date);
        if (pos >= 0 && pos < pagerAdapter.getCount()) {
            pager.setCurrentItem(pos);
        }
    }

    @UiThread
    protected void showLimitApproachedError() {
        OnlySupportedInProDialogFragment fragment = OnlySupportedInProDialogFragment.newInstance();
        fragment.setTitleResId(R.string.dialog_feature_only_in_pro_title_events_approached_limit);
        fragment.setMessageResId(R.string.dialog_feature_only_in_pro_message_events_approached_limit);
        fragment.show(getSupportFragmentManager(), null);
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
}
