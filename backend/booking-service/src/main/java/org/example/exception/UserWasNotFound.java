package org.example.exception;

public class UserWasNotFound extends RuntimeException {
    public UserWasNotFound(String message) {
        super(message);
    }
}
