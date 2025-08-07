package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.exception.KafkaTopicCreationException;
import com.autumn.loganalyzer_admin.model.KafkaAclRequest;
import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAclService;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import com.autumn.loganalyzer_admin.utility.KafkaAclPermission;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class KafkaAdminServiceImpl implements KafkaAdminService {

    private final AdminClient adminClient;
    private final KafkaAclService kafkaAclService;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value(("${spring.kafka.zookeeper.connect:localhost:2181}"))
    private String zookeeperConnect;

    @Override
    public void createTopic(KafkaAdminDTO kafkaAdminDTO) throws ExecutionException, InterruptedException {
        try {
            // Step 1: Create Kafka topic
            NewTopic newTopic = new NewTopic(kafkaAdminDTO.getTopicName(), kafkaAdminDTO.getPartitions(), kafkaAdminDTO.getReplicationFactor());
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Kafka topic created: " + kafkaAdminDTO.getTopicName());
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Failed to create Kafka topic: " + e.getMessage());
            throw new KafkaTopicCreationException(
                    "Failed to create Kafka topic: " + kafkaAdminDTO.getTopicName(), e);
        }
        System.out.println("Creating ACLs and user for topic: " + kafkaAdminDTO.getTopicName());
        try {
            // Step 2: Create ACL for topic WRITE
            KafkaAclRequest kafkaAclRequest = KafkaAclRequest.builder()
                    .topic(kafkaAdminDTO.getTopicName())
                    .resourceName(kafkaAdminDTO.getTopicName())
                    .username(kafkaAdminDTO.getUsername())
                    .permissionType(KafkaAclPermission.WRITE)
                    .resourceType(ResourceType.TOPIC)
                    .build();
            createAcls(kafkaAclRequest);

            // Step 3: Create ACL for consumer group READ
            System.out.println("Creating ACLs for consumer group: " + kafkaAdminDTO.getConsumerGroup());
            KafkaAclRequest groupAcl = KafkaAclRequest.builder()
                    .group(kafkaAdminDTO.getConsumerGroup())
                    .resourceName(kafkaAdminDTO.getConsumerGroup())
                    .username(kafkaAdminDTO.getUsername())
                    .permissionType(KafkaAclPermission.READ)
                    .resourceType(ResourceType.GROUP)
                    .build();
            createAcls(groupAcl);
        } catch (Exception e) {
            System.out.println("Failed to create ACLs: " + e.getMessage());
            throw new RuntimeException("Failed to create ACLs for topic: " + kafkaAdminDTO.getTopicName(), e);
        }
        System.out.println("ACLs created successfully for topic: " + kafkaAdminDTO.getTopicName());
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

    public void createAcls(KafkaAclRequest kafkaAclRequest) {
        kafkaAclService.addAcl(kafkaAclRequest);
    }
}
