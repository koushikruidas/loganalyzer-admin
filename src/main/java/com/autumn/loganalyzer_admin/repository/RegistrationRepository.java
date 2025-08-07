package com.autumn.loganalyzer_admin.repository;


import com.autumn.loganalyzer_admin.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByApiKey(String apiKey);
    boolean existsByKafkaTopic(String kafkaTopic);
    Optional<Registration> findByKafkaTopic(String kafkaTopic);
    Optional<Registration> findByApplicationNameAndOrganizationName(String appName, String orgName);
    List<Registration> findAllByIsActiveTrue();
}
