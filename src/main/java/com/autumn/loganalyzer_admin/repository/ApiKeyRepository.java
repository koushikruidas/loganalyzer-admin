package com.autumn.loganalyzer_admin.repository;


import com.autumn.loganalyzer_admin.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);

    Optional<ApiKey> findByApplicationNameAndOrganizationName(String appName, String orgName);
}
