package de.iweinzierl.worktrack.util;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.slf4j.Logger;

import java.util.Objects;

import de.iweinzierl.worktrack.model.Backup;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.persistence.WorkplaceRepository;

public class BackupHelper {

    public interface Callback {
        void onSuccess();

        void onFailure();
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupHelper.class.getName());

    private final TrackingItemRepository trackingItemRepository;
    private final WorkplaceRepository workplaceRepository;

    public BackupHelper(TrackingItemRepository trackingItemRepository, WorkplaceRepository workplaceRepository) {
        Objects.requireNonNull(trackingItemRepository, "trackingItemRepository must not be null");
        Objects.requireNonNull(workplaceRepository, "workplaceRepository must not be null");

        this.trackingItemRepository = trackingItemRepository;
        this.workplaceRepository = workplaceRepository;
    }

    public void importBackup(final Backup backup, final Callback callback) {
        try {
            trackingItemRepository.deleteAll();
            workplaceRepository.deleteAll();

            for (TrackingItem item : backup.getTrackingItems()) {
                item.setId(null);
                trackingItemRepository.save(item);
            }

            for (Workplace workplace : backup.getWorkplaces()) {
                workplace.setId(null);
                workplaceRepository.save(workplace);
            }

            callback.onSuccess();
        } catch (Exception e) {
            LOGGER.error("BACKUP IMPORT ERROR", e);

            if (callback != null) {
                callback.onFailure();
            }
        }
    }
}
