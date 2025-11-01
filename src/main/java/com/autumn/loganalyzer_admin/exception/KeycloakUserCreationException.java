package com.autumn.loganalyzer_admin.exception;

public class KeycloakUserCreationException extends RegistrationException {
    public KeycloakUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}