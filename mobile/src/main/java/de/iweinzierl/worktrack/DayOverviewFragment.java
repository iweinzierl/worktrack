package de.iweinzierl.worktrack;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;
import de.iweinzierl.worktrack.util.SettingsHelper;
import de.iweinzierl.worktrack.util.WorktimeCalculator;
import de.iweinzierl.worktrack.view.WorkingTimeStatisticsView;
import de.iweinzierl.worktrack.view.adapter.NoOpActionCallback;
import de.iweinzierl.worktrack.view.adapter.TrackingItemAdapter;

@EFragment(R.layout.fragment_day_overview)
public class DayOverviewFragment extends Fragment {

    private class TrackingItemActionCallback extends NoOpActionCallback<TrackingItem> {
        @Override
        public void onDeleteItem(TrackingItem item) {
            deleteItem(item);
        }
    }

    interface TrackingItemCallback {
        void onDeleteItem(TrackingItem item);
    }

    public static final String ARGS_YEAR = "dayoverviewfragment.args.year";
    public static final String ARGS_MONTH = "dayoverviewfragment.args.month";
    public static final String ARGS_DAY = "dayoverviewfragment.args.day";

    private TrackingItemCallback trackingItemCallback;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    WorkingTimeStatisticsView statisticsView;

    @ViewById
    RecyclerView cardView;

    @ColorRes(R.color.toolbarOverHours)
    int overHoursColor;

    private boolean trackingItemsCorrect;

    private TrackingItemAdapter trackingItemAdapter;

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .printZeroNever()
            .appendSuffix("h ")
            .appendMinutes()
            .printZeroAlways()
            .appendSuffix("min")
            .toFormatter();

    public DayOverviewFragment() {
        trackingItemAdapter = new TrackingItemAdapter(new TrackingItemActionCallback());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        trackingItemAdapter.setContext(context);

        if (context instanceof TrackingItemCallback) {
            trackingItemCallback = (TrackingItemCallback) context;
        }
    }

    @AfterViews
    protected void setupUI() {
        cardView.setAdapter(trackingItemAdapter);
        cardView.setHasFixedSize(false);
        cardView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Background
    protected void updateUI() {
        Bundle args = getArguments();
        if (args != null) {
            LocalDate date = new LocalDate(args.getInt(ARGS_YEAR), args.getInt(ARGS_MONTH), args.getInt(ARGS_DAY));

            List<TrackingItem> byDate = trackingItemRepository.findByDate(date);
            Collections.sort(byDate, new Comparator<TrackingItem>() {
                @Override
                public int compare(TrackingItem trackingItem, TrackingItem t1) {
                    return trackingItem.getEventTime().compareTo(t1.getEventTime());
                }
            });

            setDateView(date);
            setTrackingItems(byDate);
            determineIfItemsAreCorrect(byDate);
            displayWarningIcon(!trackingItemsCorrect);
        }
    }

    private void determineIfItemsAreCorrect(List<TrackingItem> items) {
        setTrackingItemsCorrect(true);

        // 1. check if two subsequent items are from the same type
        TrackingItem last = null;
        for (TrackingItem item : items) {
            if (last != null && last.getType() == item.getType()) {
                setTrackingItemsCorrect(false);
            }
            last = item;
        }

        // 2. check if the last item of a past day is a check-in item
        if (last != null && !last.getEventTime().toLocalDate().isEqual(LocalDate.now()) && last.getType() == TrackingItemType.CHECKIN) {
            setTrackingItemsCorrect(false);
        }
    }

    @UiThread
    protected void displayWarningIcon(boolean display) {
        /*
        if (display) {
            warningIcon.setVisibility(View.VISIBLE);
        } else {
            warningIcon.setVisibility(View.GONE);
        }
        */
    }

    @UiThread
    protected void setTrackingItems(List<TrackingItem> items) {
        trackingItemAdapter.setItems(items);
        calculateAndSetDuration(items);
    }

    @UiThread
    protected void setDateView(LocalDate date) {
        //dateView.setText(date.toString(getString(R.string.util_date_format)));
    }

    public void deleteItem(final TrackingItem item) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.activity_dayoverview_action_delete_title)
                .setNegativeButton(R.string.activity_dayoverview_action_delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.activity_dayoverview_action_delete_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trackingItemCallback.onDeleteItem(item);
                        trackingItemAdapter.removeItem(item);
                    }
                })
                .show();
    }

    private void setTrackingItemsCorrect(boolean trackingItemsCorrect) {
        this.trackingItemsCorrect = trackingItemsCorrect;
    }

    private void calculateAndSetDuration(List<TrackingItem> items) {
        final int dailyWorkingHours = new SettingsHelper(getActivity()).getDailyWorkingHours();

        Period duration = new WorktimeCalculator(items).calculate();
        /*
        durationView.setText(duration.normalizedStandard().toString(periodFormatter));

        if (duration.getHours() > dailyWorkingHours) {
            durationView.setTextColor(overHoursColor);
            durationView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        */

        statisticsView.setWorkingHours(duration);
    }
}
