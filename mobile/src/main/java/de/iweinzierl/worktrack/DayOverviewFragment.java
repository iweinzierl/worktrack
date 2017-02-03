package de.iweinzierl.worktrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.view.adapter.ItemToucheHelperAdapter;
import de.iweinzierl.worktrack.view.adapter.TrackingItemAdapter;

@EFragment(R.layout.fragment_overview)
public class DayOverviewFragment extends Fragment {

    public static final String ARGS_YEAR = "dayoverviewfragment.args.year";
    public static final String ARGS_MONTH = "dayoverviewfragment.args.month";
    public static final String ARGS_DAY = "dayoverviewfragment.args.day";

    public interface TrackingItemCallback {
        void onDeleteItem(TrackingItem item);
    }

    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private final ItemToucheHelperAdapter<TrackingItem> adapter;
        private final TrackingItemCallback trackingItemCallback;

        private ItemTouchHelperCallback(ItemToucheHelperAdapter<TrackingItem> adapter, TrackingItemCallback trackingItemCallback) {
            this.adapter = adapter;
            this.trackingItemCallback = trackingItemCallback;
        }


        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            trackingItemCallback.onDeleteItem(adapter.getItem(viewHolder.getAdapterPosition()));
            adapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    RecyclerView cardView;

    @ViewById
    TextView dateView;

    @ViewById
    TextView durationView;

    private TrackingItemAdapter trackingItemAdapter;

    private TrackingItemCallback trackingItemCallback;

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .printZeroNever()
            .appendSuffix("h ")
            .appendMinutes()
            .printZeroAlways()
            .appendSuffix("min")
            .toFormatter();

    public DayOverviewFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        trackingItemAdapter = new TrackingItemAdapter(getContext());

        if (context instanceof TrackingItemCallback) {
            trackingItemCallback = (TrackingItemCallback) context;
        }
    }

    @AfterViews
    protected void setupUI() {
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(trackingItemAdapter, trackingItemCallback));
        touchHelper.attachToRecyclerView(cardView);

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

            setTrackingItems(byDate);
        }
    }

    @UiThread
    public void setTrackingItems(List<TrackingItem> items) {
        trackingItemAdapter.setItems(items);
        cardView.setAdapter(trackingItemAdapter);

        determineAndSetDate(items);
        calculateAndSetDuration(items);
    }

    private void determineAndSetDate(List<TrackingItem> items) {
        Set<LocalDate> dates = new HashSet<>();

        for (TrackingItem item : items) {
            dates.add(item.getEventTime().toLocalDate());
        }

        if (dates.isEmpty()) {
            dateView.setText("////-//-//");
        } else if (dates.size() == 1) {
            dateView.setText(items.get(0).getEventTime().toString("yyyy-MM-dd"));
        } else {
            dateView.setText("TODO: IMPLEMENT");
        }
    }

    private void calculateAndSetDuration(List<TrackingItem> items) {
        Period duration = new Period();

        for (int idx = 0; idx < items.size(); idx += 2) {
            if (idx < items.size() - 1) {
                DateTime tA = items.get(idx).getEventTime();
                DateTime tB = items.get(idx + 1).getEventTime();

                duration = duration.plus(new Period(tA, tB));
            }
        }

        durationView.setText(duration.normalizedStandard().toString(periodFormatter));
    }
}
