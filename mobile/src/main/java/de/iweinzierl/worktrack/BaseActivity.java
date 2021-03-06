package de.iweinzierl.worktrack;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.persistence.repository.RepositoryFactory;
import de.iweinzierl.worktrack.persistence.repository.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.repository.WorkplaceRepository;
import de.iweinzierl.worktrack.view.drawer.DrawerAdapter;

@EActivity
public abstract class BaseActivity extends AppCompatActivity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("BaseActivity");

    FirebaseAnalytics firebaseAnalytics;

    DrawerLayout drawerLayout;

    ActionBarDrawerToggle drawerToggle;

    TrackingItemRepository trackingItemRepository;
    WorkplaceRepository workplaceRepository;

    @ViewById
    ListView leftDrawer;

    abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            LOGGER.warn("No support action bar given!");
        }
    }

    @Bean
    void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        trackingItemRepository = repositoryFactory.getTrackingItemRepository();
        workplaceRepository = repositoryFactory.getWorkplaceRepository();
    }

    public TrackingItemRepository getTrackingItemRepository() {
        return trackingItemRepository;
    }

    public WorkplaceRepository getWorkplaceRepository() {
        return workplaceRepository;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        leftDrawer.setAdapter(new DrawerAdapter(drawerLayout));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsParams.ACTIVITY.name(), getClass().getCanonicalName());
        firebaseAnalytics.logEvent(AnalyticsEvents.ACTIVITY_RESUMED.name(), bundle);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsParams.ACTIVITY.name(), getClass().getCanonicalName());
        firebaseAnalytics.logEvent(AnalyticsEvents.ACTIVITY_PAUSED.name(), bundle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                } else {
                    drawerLayout.openDrawer(leftDrawer);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showMessage(String message) {
        Snackbar.make(
                getWindow().getDecorView().findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG).show();
    }
}