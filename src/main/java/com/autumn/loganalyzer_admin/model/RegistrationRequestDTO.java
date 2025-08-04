package com.autumn.loganalyzer_admin.model;

import com.autumn.loganalyzer_admin.utility.RoutingPolicy;
import lombok.Data;

@Data
public class RegistrationRequestDTO {
    private String organizationName;
    private String applicationName;
    private String environment; // e.g., dev, stage, prod
    private RoutingPolicy routingPolicy;
    private boolean isShared;
}