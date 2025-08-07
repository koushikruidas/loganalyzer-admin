package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.entity.Registration;
import com.autumn.loganalyzer_admin.exception.ElasticIndexCreationException;
import com.autumn.loganalyzer_admin.exception.KafkaTopicCreationException;
import com.autumn.loganalyzer_admin.model.*;
import com.autumn.loganalyzer_admin.repository.RegistrationRepository;
import com.autumn.loganalyzer_admin.service.interfaces.ElasticAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import com.autumn.loganalyzer_admin.service.interfaces.RegistrationService;
import com.autumn.loganalyzer_admin.service.interfaces.RoutingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final KafkaAdminService kafkaAdminService;
    private final ElasticAdminService elasticAdminService;
    private final RoutingService routingService;
    private final ModelMapper modelMapper;
    @Override
    public RegistrationResponseDTO registerApplication(RegistrationRequestDTO dto) throws ExecutionException, InterruptedException {
        String apiKey = UUID.randomUUID().toString();
        String kafkaTopic = routingService.resolveTopic(dto);
        String elasticIndex = routingService.resolveIndex(dto);
        String consumerGroup = kafkaTopic+ "_consumer_group";
        String kafkaUsername = "loggen_" + dto.getOrganizationName().toLowerCase() + "_" + dto.getApplicationName().toLowerCase();

        // Check if already registered
        Optional<Registration> existingKafkaTopic = registrationRepository.findByKafkaTopic(kafkaTopic);

        if (existingKafkaTopic.isPresent()) {
            Registration existingRegistration = existingKafkaTopic.get();
            return RegistrationResponseDTO.builder()
                    .topicName(existingRegistration.getKafkaTopic())
                    .indexName(existingRegistration.getElasticIndex())
                    .kafkaUsername(existingRegistration.getKafkaUsername())
                    .consumerGroup(existingRegistration.getConsumerGroup())
                    .apiKey(existingRegistration.getApiKey())
                    .error("Registration already exists")
                    .build();
        }


        // Create Kafka Topic
        try {
            if (!kafkaAdminService.topicExists(kafkaTopic)) {
                KafkaAdminDTO kafkaDTO = KafkaAdminDTO.builder()
                        .topicName(kafkaTopic)
                        .username(kafkaUsername)
                        .consumerGroup(consumerGroup)
                        .build();
                kafkaAdminService.createTopic(kafkaDTO);
            }
        } catch (Exception e) {
            throw new KafkaTopicCreationException("Kafka topic creation failed: " + e.getMessage(), e);
        }

        // Step 2: Create Elasticsearch Index
        try {
            if (!elasticAdminService.indexExists(elasticIndex)) {
                ElasticAdminDTO elasticDTO = ElasticAdminDTO.builder().indexName(elasticIndex).build();
                elasticAdminService.createIndex(elasticDTO);
            } else {
                System.out.println("Elasticsearch index already exists: " + elasticIndex);
            }
        } catch (Exception e) {
            throw  new ElasticIndexCreationException(("Elasticsearch index creation failed: " + e.getMessage()), e);
        }

        // Convert DTO to entity
        Registration newRegistration = modelMapper.map(dto, Registration.class);
        newRegistration.setApiKey(apiKey);
        newRegistration.setKafkaTopic(kafkaTopic);
        newRegistration.setElasticIndex(elasticIndex);
        newRegistration.setKafkaUsername(kafkaUsername);
        newRegistration.setConsumerGroup(consumerGroup);
        newRegistration.setActive(true);

        registrationRepository.save(newRegistration);
        return RegistrationResponseDTO.builder()
                        .topicName(kafkaTopic)
                        .indexName(elasticIndex)
                        .kafkaUsername(kafkaUsername)
                        .consumerGroup(consumerGroup)
                        .apiKey(apiKey)
                        .build();
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
    public Optional<RegistrationResponseDTO> findByApplicationNameAndOrganizationName(String appName, String orgName) {
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
}
