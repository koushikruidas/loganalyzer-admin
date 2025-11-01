package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.entity.Registration;
import com.autumn.loganalyzer_admin.exception.RegistrationException;
import com.autumn.loganalyzer_admin.model.*;
import com.autumn.loganalyzer_admin.repository.RegistrationRepository;
import com.autumn.loganalyzer_admin.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final KafkaAdminService kafkaAdminService;
    private final ElasticAdminService elasticAdminService;
    private final RoutingService routingService;
    private final KeycloakService keycloakService;
    private final ModelMapper modelMapper;

    @Override
    public RegistrationResponseDTO registerApplication(RegistrationRequestDTO dto)
            throws ExecutionException, InterruptedException {

        validateRequest(dto);

        String apiKey = UUID.randomUUID().toString();
        String kafkaTopic = routingService.resolveTopic(dto);
        String elasticIndex = routingService.resolveIndex(dto);
        String consumerGroup = kafkaTopic + "_consumer_group";
        String kafkaUsername = "svc_" + dto.getOrganizationName().toLowerCase()
                + "_" + dto.getApplicationName().toLowerCase();

        Optional<Registration> existing = registrationRepository.findByKafkaTopic(kafkaTopic);
        if (existing.isPresent()) {
            return buildExistingResponse(existing.get());
        }
        String keycloakUserId = null;
        try {
            keycloakUserId = createKeycloakUser(dto);
            createKafkaResources(dto, kafkaTopic, kafkaUsername, consumerGroup);
            createElasticIndex(elasticIndex);

            Registration saved = saveRegistration(dto, apiKey, kafkaTopic, elasticIndex,
                    kafkaUsername, consumerGroup, keycloakUserId);

            return buildSuccessResponse(saved);

        } catch (Exception ex) {
            rollback(kafkaTopic, keycloakUserId, elasticIndex);
            throw new RegistrationException("Registration Failed: " +ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, String> getTopicToIndexMap() {
        return registrationRepository.findAllByIsActiveTrue()
                .stream()
                .collect(Collectors.toMap(
                        Registration::getKafkaTopic,
                        Registration::getElasticIndex,
                        (existing, replacement) -> existing // handle duplicate keys
                ));
    }

    @Override
    public Map<String, TopicIndexDTO> registrationMap() {
        return registrationRepository.findAllByIsActiveTrue()
                .stream()
                .collect(Collectors.toMap(
                        Registration::getKafkaTopic,
                        reg -> TopicIndexDTO.builder()
                                .kafkaUsername(reg.getKafkaUsername())
                                .consumerGroup(reg.getConsumerGroup())
                                .elasticIndex(reg.getElasticIndex())
                                .build(),
                        (existing, replacement) -> existing // handle duplicate keys
                ));
    }

    @Override
    public Optional<RegistrationResponseDTO> findByApiKey(String apiKey) {
        return registrationRepository.findByApiKey(apiKey)
                .map(registration -> modelMapper.map(registration, RegistrationResponseDTO.class));
    }

    @Override
    public Optional<RegistrationResponseDTO> findByApplicationNameAndOrganizationName(String appName, String
            orgName) {
        return registrationRepository.findByApplicationNameAndOrganizationName(appName, orgName)
                .map(registration -> modelMapper.map(registration, RegistrationResponseDTO.class));
    }

    @Override
    public List<RegistrationResponseDTO> findAll() {
        return registrationRepository.findAll()
                .stream()
                .map(registration -> modelMapper.map(registration, RegistrationResponseDTO.class))
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateRequest(RegistrationRequestDTO dto) {
        if (dto.getKeycloakUsername() == null || dto.getKeycloakUsername().isEmpty()) {
            throw new IllegalArgumentException("keycloakUsername is required");
        }
    }

    private RegistrationResponseDTO buildExistingResponse(Registration existing) {
        return RegistrationResponseDTO.builder()
                .kafkaTopic(existing.getKafkaTopic())
                .elasticIndex(existing.getElasticIndex())
                .kafkaUsername(existing.getKafkaUsername())
                .consumerGroup(existing.getConsumerGroup())
                .apiKey(existing.getApiKey())
                .error("Registration already exists")
                .build();
    }

    private String createKeycloakUser(RegistrationRequestDTO dto) {
        KeycloakUserCheckDTO check = keycloakService.checkUserExists(
                dto.getKeycloakUsername(),
                dto.getEmail()
        );

        if (check.usernameExists) {
            throw new IllegalStateException(
                    "Username already exists in Keycloak: " + dto.getKeycloakUsername()
            );
        }

        if (check.emailExists) {
            throw new IllegalStateException(
                    "Email already exists in Keycloak: " + dto.getEmail()
            );
        }


        try {
            String id = keycloakService.createUser(dto.getKeycloakUsername(), dto.getEmail(), true);
            keycloakService.setTemporaryPassword(id, RandomStringUtils.secure().nextAlphanumeric(12));
            return id;
        } catch (Exception e) {
            log.error("Failed to create keycloak user: {}",e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void createKafkaResources(RegistrationRequestDTO dto, String topic, String username, String consumerGroup) {
        try {
            if (!kafkaAdminService.topicExists(topic)) {
                kafkaAdminService.createTopic(KafkaAdminDTO.builder()
                        .topicName(topic)
                        .username(username)
                        .consumerGroup(consumerGroup)
                        .partitions(dto.getPartitions())
                        .replicationFactor(dto.getReplicationFactor())
                        .build());
                log.info("kafka topic created: {}",topic);
            }
        } catch (Exception e) {
            log.error("Failed to create kafka topic: {}",e.getMessage());
            throw new IllegalStateException("Failed to create Kafka topic: "+ e.getMessage());
        }
    }

    private void createElasticIndex(String index) {
        try {
            if (!elasticAdminService.indexExists(index)) {
                elasticAdminService.createIndex(ElasticAdminDTO.builder().indexName(index).build());
                log.info("elastic index created: {}",index);
            }
        } catch (Exception e) {
            log.error("Elasticsearch index creation failed: {}",e.getMessage());
            throw new IllegalStateException("Elasticsearch index creation failed"+ e.getMessage());
        }
    }

    private Registration saveRegistration(
            RegistrationRequestDTO dto, String apiKey, String topic, String index,
            String username, String group, String keycloakUserId) {

        Registration reg = modelMapper.map(dto, Registration.class);

        reg.setApiKey(apiKey);
        reg.setKafkaTopic(topic);
        reg.setElasticIndex(index);
        reg.setKafkaUsername(username);
        reg.setConsumerGroup(group);
        reg.setActive(true);
        reg.setKeycloakUserId(keycloakUserId);
        reg.setKeycloakUsername(dto.getKeycloakUsername());
        reg.setQuotaBytes(dto.getQuotaBytes() * 1024 * 1024 * 1024); // quotas in GB
        reg.setCreatedAt(Instant.now());

        return registrationRepository.save(reg);
    }

    private RegistrationResponseDTO buildSuccessResponse(Registration r) {
        return RegistrationResponseDTO.builder()
                .kafkaTopic(r.getKafkaTopic())
                .elasticIndex(r.getElasticIndex())
                .kafkaUsername(r.getKafkaUsername())
                .consumerGroup(r.getConsumerGroup())
                .apiKey(r.getApiKey())
                .keycloakUsername(r.getKeycloakUsername())
                .replicationFactor(r.getReplicationFactor())
                .partitions(r.getPartition())
                .email(r.getEmail())
                .quotaBytes(r.getQuotaBytes())
                .build();
    }

    private void rollback(String kafkaTopic, String keycloakUserId, String elasticIndex) {
        log.info("========= Rollback Mechanism =========");
        try {
            kafkaAdminService.deleteTopicAsync(kafkaTopic);
            log.info("Successfully deleted the kafka topic: {}",kafkaTopic);
        } catch (Exception e) {
            log.error("Failed to delete kafka topic: ",e);
        }

        if (keycloakUserId != null && !keycloakUserId.isBlank()) {
            try {
                keycloakService.deleteUser(keycloakUserId);
                log.info("Successfully deleted the keycloak userId: {}", keycloakUserId);
            } catch (Exception deleteEx) {
                log.error("Failed to delete Keycloak user during rollback: {}", deleteEx.getMessage());
            }
        } else {
            log.info("No Keycloak user to rollback.");
        }

        try {
            elasticAdminService.deleteIndex(elasticIndex);
        } catch (Exception e) {
            log.error("Failed to delete elastic index: {}", elasticIndex);
        }
    }

}
