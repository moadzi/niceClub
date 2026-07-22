package com.ogcnice.football.exception;

/**
 * Thrown when a requested resource (team or player) cannot be found by its id.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Long id) {
        super("Resource not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
