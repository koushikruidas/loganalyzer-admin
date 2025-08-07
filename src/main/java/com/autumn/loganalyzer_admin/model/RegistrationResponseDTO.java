package com.autumn.loganalyzer_admin.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegistrationResponseDTO {
    private String organizationName;
    private String applicationName;
    private String topicName;
    private String indexName;
    private String kafkaUsername;
    private String consumerGroup;
    private String apiKey;
    private boolean isActive;
    private String error;
}
