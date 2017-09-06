package de.iweinzierl.worktrack.persistence;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EBean
public class LocalWorkplaceRepository implements WorkplaceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalWorkplaceRepository.class);

    @Bean
    DaoSessionFactory sessionFactory;

    private DaoSession session;

    private DaoSession getSession() {
        if (session == null) {
            session = sessionFactory.getSession();
        }

        return session;
    }

    @Override
    public Workplace save(Workplace workplace) {
        try {
            getSession().getWorkplaceDao().insert(workplace);
            return workplace;
        } catch (Exception e) {
            LOGGER.error("Saving workplace item failed", e);
        }

        return null;
    }

    @Override
    public List<Workplace> findAll() {
        try {
            return getSession().getWorkplaceDao().loadAll();
        } catch (Exception e) {
            LOGGER.error("Finding all workplace items failed", e);
        }

        return null;
    }

    @Override
    public boolean delete(Workplace workplace) {
        try {
            getSession().getWorkplaceDao().delete(workplace);
            return true;
        } catch (Exception e) {
            LOGGER.error("Deletion of workplace failed", e);
        }

        return false;
    }
}
