package com.autumn.loganalyzer_admin.model;

import lombok.Data;

@Data
public class RegistrationDTO {
    private String organizationName;
    private String applicationName;
    private String environment; // e.g., dev, stage, prod
    private boolean sharedIndex;
}