package de.iweinzierl.worktrack.persistence;

import java.util.List;

public interface WorkplaceRepository {

    Workplace save(Workplace workplace);

    List<Workplace> findAll();

    boolean delete(Workplace workplace);
}
