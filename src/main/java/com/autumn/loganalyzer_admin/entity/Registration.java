package com.autumn.loganalyzer_admin.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_registrations")
@Data
public class Registration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organizationName;
    private String applicationName;
    private String apiKey;
    private String kafkaTopic;
    private String kafkaUsername;
    private String consumerGroup;
    private String elasticIndex;
    private boolean isActive;
}
