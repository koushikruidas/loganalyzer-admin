package com.autumn.loganalyzer_admin.repository;


import com.autumn.loganalyzer_admin.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);
    boolean existsByKafkaTopic(String kafkaTopic);
    Optional<ApiKey> findByKafkaTopic(String kafkaTopic);
    Optional<ApiKey> findByApplicationNameAndOrganizationName(String appName, String orgName);
}
