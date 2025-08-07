package com.autumn.loganalyzer_admin.exception;

public class KafkaTopicCreationException extends RuntimeException {
    public KafkaTopicCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
