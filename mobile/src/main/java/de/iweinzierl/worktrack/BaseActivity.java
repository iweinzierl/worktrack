package de.iweinzierl.worktrack;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.BackupHelper;
import de.iweinzierl.worktrack.view.dialog.BackupTitleInputDialog;
import de.iweinzierl.worktrack.view.drawer.DrawerAdapter;

@EActivity
public abstract class BaseActivity extends BaseGoogleApiActivity implements BackupHelper.BackupCallback {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("BaseActivity");

    private static final int REQUEST_BACKUP_ID = 1001;

    DrawerLayout drawerLayout;

    ActionBarDrawerToggle drawerToggle;

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @ViewById
    ListView leftDrawer;

    abstract int getLayoutId();

    public TrackingItemRepository getTrackingItemRepository() {
        return trackingItemRepository;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            LOGGER.warn("No support action bar given!");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        leftDrawer.setAdapter(new DrawerAdapter(drawerLayout));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        drawerLayout.closeDrawers();
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
            case R.id.action_create_backup:
                createBackup();
                return true;
            case R.id.action_import_backup:
                importBackup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BACKUP_ID && resultCode == RESULT_OK) {
            importBackup(data.getStringExtra(ListBackupsActivity.EXTRA_BACKUP_DRIVE_ID));
        }
    }

    private void createBackup() {
        final BackupTitleInputDialog dialog = new BackupTitleInputDialog();
        final BackupHelper backupHelper = new BackupHelper(
                this,
                getTrackingItemRepository(),
                getGoogleApiClient());

        dialog.setCallback(new BackupTitleInputDialog.Callback() {
            @Override
            public void onConfirm(String title) {
                backupHelper.writeBackup(title);
            }

            @Override
            public void onCancel() {
                // nothing to do
            }
        });

        dialog.show(getSupportFragmentManager(), null);
    }

    private void importBackup() {
        startActivityForResult(new Intent(this, ListBackupsActivity_.class), REQUEST_BACKUP_ID);
    }

    private void importBackup(String backupDriveId) {
        new BackupHelper(
                this,
                trackingItemRepository,
                getGoogleApiClient()
        ).importBackup(backupDriveId);
    }

    @Override
    public void onListBackups(List<BackupMetaData> backups) {
    }

    @Override
    public void onCreationSuccessful() {
        showMessage("BackupMetaData creation successful");
    }

    @Override
    public void onCreationFailed() {
        showMessage("BackupMetaData creation failed");
    }

    @Override
    public void onImportSuccessful() {
        showMessage("BackupMetaData import successful");
    }

    @Override
    public void onImportFailed() {
        showMessage("BackupMetaData import failed");
    }

    protected void showMessage(String message) {
        Snackbar.make(
                getWindow().getDecorView().findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG).show();
    }
}