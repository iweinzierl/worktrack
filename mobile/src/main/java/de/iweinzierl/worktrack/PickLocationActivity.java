package de.iweinzierl.worktrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.androidannotations.annotations.EActivity;
import org.slf4j.Logger;

@EActivity
public class PickLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("PickLocationActivity");

    private static final int REQUEST_SETUP_MAPS = 200;

    public static final int REQUEST_LOCATION = 100;

    public static final String EXTRA_TITLE = "PickLocationActivity.extra.title";
    public static final String EXTRA_LAT = "PickLocationActivity.extra.lat";
    public static final String EXTRA_LON = "PickLocationActivity.extra.lon";
    public static final String EXTRA_RADIUS = "PickLocationActivity.extra.radius";

    private FusedLocationProviderClient locationProviderClient;
    private GoogleMap googleMap;
    private Circle selectedLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);
        setTitle(R.string.activity_pick_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.activity_pick_location);
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_SETUP_MAPS);
        }

        setupMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pick_location, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pick_location) {
            finishSelection();
            return true;
        }

        return false;
    }

    protected void finishSelection() {
        LOGGER.info("clicked action: select location");

        if (selectedLocation != null) {
            Intent data = new Intent();
            data.putExtra(EXTRA_LAT, selectedLocation.getCenter().latitude);
            data.putExtra(EXTRA_LON, selectedLocation.getCenter().longitude);
            data.putExtra(EXTRA_RADIUS, selectedLocation.getRadius());

            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SETUP_MAPS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMap();
        }
    }

    private void setupMap() {
        googleMap.setTrafficEnabled(false);
        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setSelection(latLng);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            googleMap.setMyLocationEnabled(true);

            locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            latLng,
                            10
                    ));
                    setSelection(latLng);
                }
            });
        }
    }

    private void setSelection(LatLng latLng) {
        if (selectedLocation == null) {
            CircleOptions selection = new CircleOptions()
                    .fillColor(R.color.colorAccent)
                    .center(latLng)
                    .radius(100d);
            selectedLocation = googleMap.addCircle(selection);
        }

        selectedLocation.setCenter(latLng);
    }
}
