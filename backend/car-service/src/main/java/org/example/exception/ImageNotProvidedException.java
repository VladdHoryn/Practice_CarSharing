package org.example.exception;

public class ImageNotProvidedException extends RuntimeException {
    public ImageNotProvidedException(String message) {
        super(message);
    }
}
