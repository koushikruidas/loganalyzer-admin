package com.autumn.loganalyzer_admin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaAdminDTO {
    private String topicName;
    @Builder.Default
    private int partitions = 1; // Default partitions
    @Builder.Default
    private short replicationFactor = 1; // Default replication
    private String username; // Username for ACL operations
    private String consumerGroup;
}
