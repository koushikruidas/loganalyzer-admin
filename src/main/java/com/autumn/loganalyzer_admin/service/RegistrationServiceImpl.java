package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.entity.ApiKey;
import com.autumn.loganalyzer_admin.model.ElasticAdminDTO;
import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;
import com.autumn.loganalyzer_admin.model.RegistrationDTO;
import com.autumn.loganalyzer_admin.repository.ApiKeyRepository;
import com.autumn.loganalyzer_admin.service.interfaces.ElasticAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.RegistrationService;
import com.autumn.loganalyzer_admin.service.interfaces.RoutingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ApiKeyRepository apiKeyRepository;
    private final KafkaAdminService kafkaAdminService;
    private final ElasticAdminService elasticAdminService;
    private RoutingService routingService;
    private final ModelMapper modelMapper;
    @Override
    public ResponseEntity<String> registerApplication(RegistrationDTO dto) throws ExecutionException, InterruptedException {
        String apiKey = UUID.randomUUID().toString();
        String kafkaTopic = routingService.resolveTopic(dto);
        String elasticIndex = routingService.resolveIndex(dto);
        String kafkaUsername = "loggen_" + dto.getOrganizationName().toLowerCase() + "_" + dto.getApplicationName().toLowerCase();

        // Check if already registered
        if (apiKeyRepository.existsByKafkaTopic(kafkaTopic)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Application is already registered with topic: " + kafkaTopic);
        }

        // Create Kafka Topic
        try {
            if (!kafkaAdminService.topicExists(kafkaTopic)) {
                KafkaAdminDTO kafkaDTO = KafkaAdminDTO.builder()
                        .topicName(kafkaTopic)
                        .username(kafkaUsername)
                        .build();
                kafkaDTO.setTopicName(kafkaTopic);
                kafkaAdminService.createTopic(kafkaDTO);
            } else {
                System.out.println("Kafka topic already exists: " + kafkaTopic);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kafka topic creation failed: " + e.getMessage());
        };

        // Step 2: Create Elasticsearch Index
        try {
            if (!elasticAdminService.indexExists(elasticIndex)) {
                ElasticAdminDTO elasticDTO = new ElasticAdminDTO();
                elasticDTO.setIndexName(elasticIndex);
                elasticAdminService.createIndex(elasticDTO);
            } else {
                System.out.println("Elasticsearch index already exists: " + elasticIndex);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Elasticsearch index creation failed: " + e.getMessage());
        }

        // Convert DTO to entity
        ApiKey newApiKey = modelMapper.map(dto, ApiKey.class);
        newApiKey.setApiKey(apiKey);
        newApiKey.setKafkaTopic(kafkaTopic);
        newApiKey.setElasticIndex(elasticIndex);
        newApiKey.setActive(true);
        apiKeyRepository.save(newApiKey);
        return ResponseEntity.ok("Registered " + dto.getApplicationName() + " -> API Key: " + apiKey);
    }
}
