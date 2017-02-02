package de.iweinzierl.worktrack;

import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
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
public class OverviewActivity extends BaseActivity {

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
    int getLayoutId() {
        return R.layout.activity_overview;
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
                return super.onOptionsItemSelected(item);
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

        LocalDate mon = DateTime.now().toLocalDate().dayOfWeek().withMinimumValue();
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 7, 55), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 12, 10), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 13, 25), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(mon.getYear(), mon.getMonthOfYear(), mon.getDayOfMonth(), 18, 20), CreationType.AUTO));

        LocalDate tue = mon.plusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 8, 30), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 12, 25), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 13, 5), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(tue.getYear(), tue.getMonthOfYear(), tue.getDayOfMonth(), 18, 9), CreationType.AUTO));

        LocalDate wed = tue.plusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 8, 53), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 13, 10), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 13, 28), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(wed.getYear(), wed.getMonthOfYear(), wed.getDayOfMonth(), 19, 5), CreationType.AUTO));

        LocalDate thu = wed.plusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 5, 50), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 12, 7), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 13, 5), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(thu.getYear(), thu.getMonthOfYear(), thu.getDayOfMonth(), 20, 10), CreationType.AUTO));

        LocalDate fri = thu.plusDays(1);
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 9, 2), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 13, 7), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 14, 0), CreationType.AUTO));
        trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKOUT, new DateTime(fri.getYear(), fri.getMonthOfYear(), fri.getDayOfMonth(), 15, 38), CreationType.AUTO));

        Snackbar.make(findViewById(android.R.id.content), "Added demo data", BaseTransientBottomBar.LENGTH_SHORT).show();

        refreshData();
    }
}
