package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaAdminServiceImpl implements KafkaAdminService {

    private final AdminClient adminClient;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public void createTopic(KafkaAdminDTO kafkaAdminDTO) throws ExecutionException, InterruptedException {
        try {
            NewTopic newTopic = new NewTopic(kafkaAdminDTO.getTopicName(), kafkaAdminDTO.getPartitions(), kafkaAdminDTO.getReplicationFactor());
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Kafka topic created: " + kafkaAdminDTO.getTopicName());
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Failed to create Kafka topic: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean topicExists(String topicName) throws ExecutionException, InterruptedException {
        try {
            return adminClient.listTopics().names().get().contains(topicName);
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Failed to check if topic exists: " + e.getMessage());
            throw e;
        }
    }
}
