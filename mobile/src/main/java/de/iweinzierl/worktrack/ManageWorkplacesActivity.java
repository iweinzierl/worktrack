package de.iweinzierl.worktrack;

import android.content.Intent;

import com.github.clans.fab.FloatingActionButton;
import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.persistence.Workplace;

@EActivity
public class ManageWorkplacesActivity extends BaseActivity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("ManageWorkplacesActivity");

    @ViewById(R.id.addAction)
    protected FloatingActionButton addAction;

    @Override
    int getLayoutId() {
        return R.layout.activity_manage_workplaces;
    }

    @Click(R.id.addAction)
    protected void clickedAddAction() {
        LOGGER.debug("clicked action button: add workplace");
        startActivityForResult(new Intent(this, PickLocationActivity_.class), PickLocationActivity.REQUEST_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PickLocationActivity.REQUEST_LOCATION && resultCode == RESULT_OK) {
            addWorkplace(
                    data.getStringExtra(PickLocationActivity.EXTRA_TITLE),
                    data.getDoubleExtra(PickLocationActivity.EXTRA_LAT, Double.NaN),
                    data.getDoubleExtra(PickLocationActivity.EXTRA_LON, Double.NaN),
                    data.getDoubleExtra(PickLocationActivity.EXTRA_RADIUS, Double.NaN)
            );
        }
    }

    private void addWorkplace(String title, double lat, double lon, double radius) {
        Workplace workplace = new Workplace(null, title, lat, lon, radius);
        LOGGER.info("Received new workplace: {}", workplace);
    }
}
