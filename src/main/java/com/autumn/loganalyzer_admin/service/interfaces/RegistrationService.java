package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.RegistrationRequestDTO;
import com.autumn.loganalyzer_admin.model.RegistrationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface RegistrationService {
    RegistrationResponseDTO registerApplication(RegistrationRequestDTO dto) throws ExecutionException, InterruptedException;
    Map<String, String> getTopicToIndexMap();
    Optional<RegistrationResponseDTO> findByApiKey(String apiKey);
    Optional<RegistrationResponseDTO> findByApplicationNameAndOrganizationName(String appName, String orgName);
    List<RegistrationResponseDTO> findAll();
}
