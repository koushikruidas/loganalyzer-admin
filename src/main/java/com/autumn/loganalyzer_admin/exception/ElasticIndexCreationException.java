package com.autumn.loganalyzer_admin.exception;

public class ElasticIndexCreationException extends RuntimeException {
    public ElasticIndexCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
