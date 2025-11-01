package com.autumn.loganalyzer_admin.model;

import com.autumn.loganalyzer_admin.utility.RoutingPolicy;
import lombok.Data;

import java.time.Instant;

@Data
public class RegistrationRequestDTO {
    private String organizationName;
    private String applicationName;
    private String environment; // e.g., dev, stage, prod
    private RoutingPolicy routingPolicy;
    private boolean isShared;
    private String keycloakUsername;
    private Long quotaBytes;
    private String email;
    private int partitions;
    private Short replicationFactor;
    private Instant createdAt;
}