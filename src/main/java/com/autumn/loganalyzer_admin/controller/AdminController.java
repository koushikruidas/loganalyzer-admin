package com.autumn.loganalyzer_admin.controller;

import com.autumn.loganalyzer_admin.entity.ApiKey;
import com.autumn.loganalyzer_admin.model.*;
import com.autumn.loganalyzer_admin.repository.ApiKeyRepository;
import com.autumn.loganalyzer_admin.service.interfaces.ApiKeyService;
import com.autumn.loganalyzer_admin.service.interfaces.ElasticAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ApiKeyService apiKeyService;
    private final ApiKeyRepository apiKeyRepository;
    private final RegistrationService registrationService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<String> registerApplication(@RequestBody RegistrationDTO registrationDTO)
            throws ExecutionException, InterruptedException {
        return registrationService.registerApplication(registrationDTO);
    }


    /**
     * Get application details by API Key or appName & orgName
     */

    @GetMapping("/application/details")
    public ResponseEntity<?> getApplicationDetails(
            @RequestParam(required = false) String apiKey,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String orgName) {

        Optional<ApiKey> apiKeyDetails;

        if (apiKey != null) {
            apiKeyDetails = apiKeyRepository.findByApiKey(apiKey);
        } else if (appName != null && orgName != null) {
            apiKeyDetails = apiKeyRepository.findByApplicationNameAndOrganizationName(appName, orgName);
        } else {
            return ResponseEntity.badRequest().body("Provide either apiKey or both appName & orgName");
        }

        if (apiKeyDetails.isPresent()) {
            return ResponseEntity.ok(modelMapper.map(apiKeyDetails.get(), ApiKeyDTO.class)); // Return ApiKey on success
        } else {
            // Return an error object or a String with an error status
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("API Key not found");
        }
    }

    @GetMapping("/resolve-topic/{topicName}")
    public ResponseEntity<TopicIndexDTO> resolveTopic(@PathVariable String topicName) {
        Optional<TopicIndexDTO> dto = apiKeyService.getTopicIndexMapping(topicName);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List all registered applications.
     */
    @GetMapping("/applications")
    public ResponseEntity<List<ApiKey>> getAllApplications() {
        return ResponseEntity.ok(apiKeyRepository.findAll());
    }
}
