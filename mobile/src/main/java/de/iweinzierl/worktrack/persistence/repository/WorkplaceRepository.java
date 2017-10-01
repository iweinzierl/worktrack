package de.iweinzierl.worktrack.persistence.repository;

import java.util.List;

import de.iweinzierl.worktrack.persistence.Workplace;

public interface WorkplaceRepository {

    Workplace save(Workplace workplace);

    List<Workplace> findAll();

    boolean delete(Workplace workplace);

    void deleteAll();
}
