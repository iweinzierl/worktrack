package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.joda.time.DateTime;

import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

@EActivity
public class OverviewActivity extends AppCompatActivity {

    @Bean(LocalTrackingItemRepository.class)
    protected TrackingItemRepository trackingItemRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrackingItem();
            }
        });
    }

    private void addTrackingItem() {
        TrackingItem item = trackingItemRepository.save(new TrackingItem(TrackingItemType.CHECKIN, DateTime.now()));

        Snackbar.make(findViewById(android.R.id.content), "Persisted item: " + item.getId(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
