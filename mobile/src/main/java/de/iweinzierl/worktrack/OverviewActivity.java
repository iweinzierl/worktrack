package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.DaoSessionFactory;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

@EActivity
public class OverviewActivity extends AppCompatActivity {

    @Bean
    DaoSessionFactory sessionFactory;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @FragmentById(R.id.fragment)
    OverviewActivityFragment fragment;

    @ViewById
    FloatingActionMenu actionMenu;

    @ViewById
    FloatingActionButton checkinAction;

    @ViewById
    FloatingActionButton checkoutAction;

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

    @Click(R.id.checkinAction)
    protected void checkinManually() {
        saveTrackingItem(new TrackingItem(TrackingItemType.CHECKIN, DateTime.now(), CreationType.MANUAL));
        refreshData();
        closeActionMenu();
    }

    @Click(R.id.checkoutAction)
    protected void checkoutManually() {
        saveTrackingItem(new TrackingItem(TrackingItemType.CHECKOUT, DateTime.now(), CreationType.MANUAL));
        refreshData();
        closeActionMenu();
    }

    @UiThread
    protected void closeActionMenu() {
        actionMenu.close(true);
    }

    @Background
    protected void saveTrackingItem(TrackingItem item) {
        trackingItemRepository.save(item);
    }

    @Background
    protected void refreshData() {
        List<TrackingItem> todaysItems = trackingItemRepository.findByDate(LocalDate.now());
        Collections.sort(todaysItems, new Comparator<TrackingItem>() {
            @Override
            public int compare(TrackingItem trackingItem, TrackingItem t1) {
                return trackingItem.getEventTime().compareTo(t1.getEventTime());
            }
        });
        updateUi(todaysItems);
    }

    @UiThread
    protected void updateUi(List<TrackingItem> items) {
        fragment.setTrackingItems(items);
    }

    private void addDemoData() {
        trackingItemRepository.deleteAll();

        DateTime now = DateTime.now();

        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 7, 55), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 12, 10), CreationType.MANUAL));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 13, 25), CreationType.MANUAL));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 18, 20), CreationType.AUTO));

        Snackbar.make(findViewById(android.R.id.content), "Added demo data", BaseTransientBottomBar.LENGTH_SHORT).show();

        refreshData();
    }
}
