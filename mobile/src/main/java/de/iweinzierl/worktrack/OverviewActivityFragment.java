package de.iweinzierl.worktrack;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.view.adapter.TrackingItemAdapter;

@EFragment(R.layout.fragment_overview)
public class OverviewActivityFragment extends Fragment {

    @ViewById
    RecyclerView cardView;

    @ViewById
    TextView dateView;

    @ViewById
    TextView durationView;

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours()
            .printZeroNever()
            .appendSuffix("h ")
            .appendMinutes()
            .printZeroAlways()
            .appendSuffix("min")
            .toFormatter();

    public OverviewActivityFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        cardView.setHasFixedSize(false);
        cardView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setTrackingItems(List<TrackingItem> items) {
        cardView.setAdapter(new TrackingItemAdapter(getContext(), items));

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
        }
        else if (dates.size() == 1) {
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
