package de.iweinzierl.worktrack;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.github.clans.fab.FloatingActionButton;
import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

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

import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.persistence.repository.LocalWorkplaceRepository;
import de.iweinzierl.worktrack.service.UpdateGeofenceService_;
import de.iweinzierl.worktrack.view.adapter.NoOpActionCallback;
import de.iweinzierl.worktrack.view.adapter.WorkplaceAdapter;
import de.iweinzierl.worktrack.view.dialog.OnlySupportedInProDialogFragment;
import de.iweinzierl.worktrack.view.dialog.WorkplaceTitleQueryDialog;

@EActivity
public class ManageWorkplacesActivity extends BaseActivity {

    private class WorkplaceActionCallback extends NoOpActionCallback<Workplace> {
        @Override
        public void onRenameItem(Workplace item) {
            renameItem(item);
        }

        @Override
        public void onDeleteItem(Workplace item) {
            deleteItem(item);
        }
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("ManageWorkplacesActivity");

    private static final int ALLOWED_WORKPLACES_IN_FREE = 1;
    private static final int REQUEST_UPDATE_GEOFENCES = 100;

    @Bean(LocalWorkplaceRepository.class)
    protected LocalWorkplaceRepository workplaceRepository;

    @ViewById(R.id.addAction)
    protected FloatingActionButton addAction;

    @ViewById
    protected ProgressBar progressBar;

    @ViewById
    protected RecyclerView cardView;

    @ViewById
    protected View emptyView;

    private WorkplaceAdapter workplaceAdapter;

    @Override
    int getLayoutId() {
        return R.layout.activity_manage_workplaces;
    }

    @AfterViews
    protected void setup() {
        workplaceAdapter = new WorkplaceAdapter(new WorkplaceActionCallback());

        cardView.setAdapter(workplaceAdapter);
        cardView.setHasFixedSize(false);
        cardView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Click(R.id.addAction)
    protected void clickedAddAction() {
        LOGGER.debug("clicked action button: add workplace");
        if (!WorktrackApplication.getInstance().isPro()
                && workplaceRepository.findAll().size() >= ALLOWED_WORKPLACES_IN_FREE) {
            OnlySupportedInProDialogFragment fragment = OnlySupportedInProDialogFragment.newInstance();
            fragment.setTitleResId(R.string.dialog_feature_only_in_pro_title_only_one_workplace);
            fragment.setMessageResId(R.string.dialog_feature_only_in_pro_message_only_one_workplace);
            fragment.show(getSupportFragmentManager(), null);
        } else {
            startActivityForResult(new Intent(this, PickLocationActivity_.class), PickLocationActivity.REQUEST_LOCATION);
        }
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
    protected void updateEmptyView() {
        if (workplaceAdapter.getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            cardView.setVisibility(View.GONE);
        }
    }

    @UiThread
    protected void setWorkplaces(List<Workplace> workplaces) {
        workplaceAdapter.setWorkplaces(workplaces);
        updateEmptyView();
    }

    @UiThread
    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @UiThread
    protected void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @UiThread
    public void renameItem(final Workplace workplace) {
        new WorkplaceTitleQueryDialog(
                workplace,
                this,
                new WorkplaceTitleQueryDialog.Callback() {
                    @Override
                    public void onSubmit(Workplace workplace) {
                        renameWorkplace(workplace);
                        workplaceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel(Workplace workplace) {
                        // do nothing
                    }
                }
        ).show();
    }

    public void deleteItem(final Workplace workplace) {
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
    protected void renameWorkplace(Workplace workplace) {
        showProgressBar();
        workplaceRepository.save(workplace);
        hideProgressBar();
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

        UpdateGeofenceService_.intent(this).updateAction().start();
    }

    private void removeGeofence(Workplace workplace) {
        UpdateGeofenceService_.intent(this).deleteAction(workplace.getGeofenceRequestId()).start();
    }
}
