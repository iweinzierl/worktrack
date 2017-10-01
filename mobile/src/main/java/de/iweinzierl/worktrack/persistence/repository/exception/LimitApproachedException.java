package de.iweinzierl.worktrack.persistence.repository.exception;

public class LimitApproachedException extends PersistenceException {

    public LimitApproachedException(String message) {
        super(message);
    }
}
