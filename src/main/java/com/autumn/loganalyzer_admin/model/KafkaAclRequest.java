package com.autumn.loganalyzer_admin.model;

import com.autumn.loganalyzer_admin.utility.KafkaAclPermission;
import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.resource.ResourceType;
@Data
@Builder
public class KafkaAclRequest {
    private String topic;
    private String clusterName;
    private String resourceName;
    private String username;
    private KafkaAclPermission permissionType;
    private ResourceType resourceType;
}