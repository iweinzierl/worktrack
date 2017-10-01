package de.iweinzierl.worktrack.persistence.repository.free;

import android.preference.PreferenceManager;

import org.androidannotations.annotations.EBean;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.repository.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.repository.exception.LimitApproachedException;
import de.iweinzierl.worktrack.persistence.repository.exception.PersistenceException;

@EBean
public class FreeTrackingItemRepository extends LocalTrackingItemRepository {

    private static final String PREFS_FIRST_DATE = "freetrackingitemrepository.date.first";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final int ALLOWED_TRACKING_DAYS = 30;

    @Override
    public TrackingItem save(TrackingItem item) throws PersistenceException {
        if (getSession().getTrackingItemDao().count() == 0) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(PREFS_FIRST_DATE, DateTime.now().toString(DATE_PATTERN))
                    .apply();
        } else {
            String firstDateStr = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREFS_FIRST_DATE, null);
            DateTime firstDate = DateTime.parse(firstDateStr, DateTimeFormat.forPattern(DATE_PATTERN));

            if (firstDate.plusDays(ALLOWED_TRACKING_DAYS).isBeforeNow()) {
                throw new LimitApproachedException(firstDateStr);
            }
        }

        return super.save(item);
    }
}
