package com.autumn.loganalyzer_admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDTO {

    private String organizationName;
    private String applicationName;
    private String apiKey;
    private String kafkaTopic;
    private String elasticIndex;
    private boolean active;
}