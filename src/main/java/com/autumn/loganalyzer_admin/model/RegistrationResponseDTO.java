package com.autumn.loganalyzer_admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDTO {
    private String organizationName;
    private String applicationName;
    private String kafkaTopic;
    private String elasticIndex;
    private String kafkaUsername;
    private String consumerGroup;
    private String apiKey;
    private boolean isActive;
    private String error;
}
