package com.autumn.loganalyzer_admin.controller;

import com.autumn.loganalyzer_admin.model.RegistrationRequestDTO;
import com.autumn.loganalyzer_admin.model.RegistrationResponseDTO;
import com.autumn.loganalyzer_admin.model.TopicIndexDTO;
import com.autumn.loganalyzer_admin.service.interfaces.ApiKeyService;
import com.autumn.loganalyzer_admin.service.interfaces.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ApiKeyService apiKeyService;
    private final RegistrationService registrationService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> registerApplication(@RequestBody RegistrationRequestDTO registrationRequestDTO)
            throws ExecutionException, InterruptedException {
        RegistrationResponseDTO response = registrationService.registerApplication(registrationRequestDTO);

        if (response.getError() != null && !response.getError().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Get application details by API Key or appName & orgName
     */

    @GetMapping("/application/details")
    public ResponseEntity<?> getApplicationDetails(
            @RequestParam(required = false) String apiKey,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String orgName) {

        Optional<RegistrationResponseDTO> registration;

        if (apiKey != null && !apiKey.isEmpty()) {
            registration = registrationService.findByApiKey(apiKey);
        } else if (appName != null && orgName != null && !appName.isEmpty() && !orgName.isEmpty()) {
            registration = registrationService.findByApplicationNameAndOrganizationName(appName, orgName);
        } else {
            return ResponseEntity.badRequest().body("Provide either apiKey or both appName & orgName");
        }

        if (registration.isPresent()) {
            return ResponseEntity.ok(modelMapper.map(registration.get(), RegistrationResponseDTO.class)); // Return ApiKey on success
        } else {
            // Return an error object or a String with an error status
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not avialable");
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
    public ResponseEntity<List<RegistrationResponseDTO>> getAllApplications() {
        return ResponseEntity.ok(registrationService.findAll());
    }

    @GetMapping("/topic-index-map")
    public ResponseEntity<Map<String, String>> getTopicIndexMap() {
        Map<String, String> map = registrationService.getTopicToIndexMap();
        return ResponseEntity.ok(map);
    }
}
