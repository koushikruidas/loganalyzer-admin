package com.autumn.loganalyzer_admin.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaAdminDTO {
    private String topicName;
    private int partitions = 3; // Default partitions
    private short replicationFactor = 1; // Default replication
    private String username; // Username for ACL operations
}
