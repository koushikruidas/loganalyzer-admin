package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.entity.Registration;
import com.autumn.loganalyzer_admin.model.TopicIndexDTO;
import com.autumn.loganalyzer_admin.repository.RegistrationRepository;
import com.autumn.loganalyzer_admin.service.interfaces.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
    private final RegistrationRepository registrationRepository;
    @Override
    public Optional<TopicIndexDTO> getTopicIndexMapping(String topicName) {
        Optional<Registration> byKafkaTopic = registrationRepository.findByKafkaTopic(topicName);
        if (byKafkaTopic.isPresent()) {
            Registration registration = byKafkaTopic.get();
            return Optional.ofNullable(TopicIndexDTO.builder()
                    .elasticIndex(registration.getElasticIndex())
                    .kafkaTopic(registration.getKafkaTopic())
                    .build());
        }
        return Optional.empty();
    }
}
