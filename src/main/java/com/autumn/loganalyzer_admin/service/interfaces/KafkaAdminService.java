package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;

import java.util.concurrent.ExecutionException;

public interface KafkaAdminService {
    void createTopic(KafkaAdminDTO kafkaAdminDTO) throws ExecutionException, InterruptedException;
    boolean topicExists(String topicName) throws ExecutionException, InterruptedException;
}
