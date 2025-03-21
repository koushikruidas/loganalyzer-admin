package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaAdminServiceImpl implements KafkaAdminService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public void createTopic(KafkaAdminDTO kafkaAdminDTO) {
        try (AdminClient adminClient = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {
            NewTopic newTopic = new NewTopic(kafkaAdminDTO.getTopicName(), kafkaAdminDTO.getPartitions(), kafkaAdminDTO.getReplicationFactor());
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Kafka topic created: " + kafkaAdminDTO.getTopicName());
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Failed to create Kafka topic: " + e.getMessage());
        }
    }
}
