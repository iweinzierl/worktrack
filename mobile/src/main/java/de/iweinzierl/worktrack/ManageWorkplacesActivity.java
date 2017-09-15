package de.iweinzierl.worktrack;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.github.clans.fab.FloatingActionButton;
import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

import de.iweinzierl.worktrack.persistence.LocalWorkplaceRepository;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.receiver.GeofencingTransitionService;
import de.iweinzierl.worktrack.view.adapter.ActionCallback;
import de.iweinzierl.worktrack.view.adapter.WorkplaceAdapter;
import de.iweinzierl.worktrack.view.dialog.WorkplaceTitleQueryDialog;

@EActivity
public class ManageWorkplacesActivity extends BaseActivity implements ActionCallback<Workplace> {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("ManageWorkplacesActivity");

    private static final int REQUEST_UPDATE_GEOFENCES = 100;

    private GeofencingClient geofencingClient;

    @Bean(LocalWorkplaceRepository.class)
    protected LocalWorkplaceRepository workplaceRepository;

    @ViewById(R.id.addAction)
    protected FloatingActionButton addAction;

    @ViewById
    protected ProgressBar progressBar;

    @ViewById
    protected RecyclerView cardView;

    private PendingIntent geofencePendingIntent;

    private WorkplaceAdapter workplaceAdapter;

    @Override
    int getLayoutId() {
        return R.layout.activity_manage_workplaces;
    }

    @AfterViews
    protected void setup() {
        geofencingClient = LocationServices.getGeofencingClient(this);
        workplaceAdapter = new WorkplaceAdapter(this);

        cardView.setAdapter(workplaceAdapter);
        cardView.setHasFixedSize(false);
        cardView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Click(R.id.addAction)
    protected void clickedAddAction() {
        LOGGER.debug("clicked action button: add workplace");
        startActivityForResult(new Intent(this, PickLocationActivity_.class), PickLocationActivity.REQUEST_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PickLocationActivity.REQUEST_LOCATION && resultCode == RESULT_OK) {
            new WorkplaceTitleQueryDialog(
                    new Workplace(
                            data.getDoubleExtra(PickLocationActivity.EXTRA_LAT, Double.NaN),
                            data.getDoubleExtra(PickLocationActivity.EXTRA_LON, Double.NaN),
                            data.getDoubleExtra(PickLocationActivity.EXTRA_RADIUS, Double.NaN),
                            UUID.randomUUID().toString()
                    ),
                    this,
                    new WorkplaceTitleQueryDialog.Callback() {
                        @Override
                        public void onSubmit(Workplace workplace) {
                            addWorkplace(workplace);
                        }

                        @Override
                        public void onCancel(Workplace workplace) {
                            // do nothing
                        }
                    }
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_UPDATE_GEOFENCES && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateGeofences(false);
        }
    }

    @Background
    protected void updateUI() {
        setWorkplaces(workplaceRepository.findAll());
    }

    @UiThread
    protected void setWorkplaces(List<Workplace> workplaces) {
        workplaceAdapter.setWorkplaces(workplaces);
    }

    @UiThread
    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @UiThread
    protected void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRenameItem(Workplace workplace) {
        new WorkplaceTitleQueryDialog(
                workplace,
                this,
                new WorkplaceTitleQueryDialog.Callback() {
                    @Override
                    public void onSubmit(Workplace workplace) {
                        workplaceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel(Workplace workplace) {
                        // do nothing
                    }
                }
        ).show();
    }

    @Override
    public void onDeleteItem(final Workplace workplace) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_manage_workplaces_dialog_delete_title)
                .setPositiveButton(R.string.activity_manage_workplaces_dialog_delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeWorkplace(workplace);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.activity_manage_workplaces_dialog_delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Background
    protected void removeWorkplace(Workplace workplace) {
        showProgressBar();
        workplaceRepository.delete(workplace);
        removeGeofence(workplace);
        updateUI();

        hideProgressBar();
    }

    @Background
    protected void addWorkplace(Workplace workplace) {
        LOGGER.info("Received new workplace: {}", workplace);

        workplaceRepository.save(workplace);
        updateGeofences(true);

        updateUI();
    }

    private void updateGeofences(boolean requestPermission) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (requestPermission) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_UPDATE_GEOFENCES);
            }

            return;
        }

        showProgressBar();

        List<Workplace> workplaces = workplaceRepository.findAll();
        GeofencingRequest geofencingRequest = buildGeofencingRequest(workplaces);

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        LOGGER.info("Adding geofences finished successfully");
                        hideProgressBar();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LOGGER.warn("Adding geofences failed", e);
                        hideProgressBar();
                    }
                });
    }

    private void removeGeofence(Workplace workplace) {
        geofencingClient.removeGeofences(Lists.newArrayList(workplace.getGeofenceRequestId()));
    }

    private Geofence buildGeofence(Workplace workplace) {
        return new Geofence.Builder()
                .setRequestId(workplace.getGeofenceRequestId())
                .setCircularRegion(
                        workplace.getLat(),
                        workplace.getLon(),
                        (float) workplace.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest buildGeofencingRequest(List<Workplace> workplaces) {
        List<Geofence> geofences = Lists.transform(workplaces, new Function<Workplace, Geofence>() {
            @Nullable
            @Override
            public Geofence apply(Workplace workplace) {
                return buildGeofence(workplace);
            }
        });

        return new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofencingTransitionService.class);
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}
