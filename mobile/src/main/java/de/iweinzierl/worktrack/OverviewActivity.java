package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.persistence.DaoSessionFactory;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

@EActivity
public class OverviewActivity extends AppCompatActivity {

    @Bean
    protected DaoSessionFactory sessionFactory;

    @Bean(LocalTrackingItemRepository.class)
    protected TrackingItemRepository trackingItemRepository;

    @FragmentById(R.id.fragment)
    protected OverviewActivityFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        refreshData();
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
            default:
                return false;
        }
    }

    private void refreshData() {
        List<TrackingItem> todaysItems = trackingItemRepository.findByDate(LocalDate.now());
        Collections.sort(todaysItems, new Comparator<TrackingItem>() {
            @Override
            public int compare(TrackingItem trackingItem, TrackingItem t1) {
                return trackingItem.getEventTime().compareTo(t1.getEventTime());
            }
        });
        fragment.setTrackingItems(todaysItems);
    }

    private void addDemoData() {
        trackingItemRepository.deleteAll();

        DateTime now = DateTime.now();

        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 7, 55)));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 12, 10)));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 13, 25)));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 18, 20)));

        Snackbar.make(findViewById(android.R.id.content), "Added demo data", BaseTransientBottomBar.LENGTH_SHORT).show();

        refreshData();
    }
}
