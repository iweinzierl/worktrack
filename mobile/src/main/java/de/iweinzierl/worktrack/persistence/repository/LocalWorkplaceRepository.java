package de.iweinzierl.worktrack.persistence.repository;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import de.iweinzierl.worktrack.persistence.DaoSession;
import de.iweinzierl.worktrack.persistence.DaoSessionFactory;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.persistence.WorkplaceDao;

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
            WorkplaceDao workplaceDao = getSession().getWorkplaceDao();

            if (workplace.getId() != null) {
                workplaceDao.update(workplace);
            } else {
                workplaceDao.insert(workplace);
            }

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

    @Override
    public void deleteAll() {
        try {
            getSession().getWorkplaceDao().deleteAll();
        } catch (Exception e) {
            LOGGER.error("Delete of all workplaces failed", e);
        }
    }
}
