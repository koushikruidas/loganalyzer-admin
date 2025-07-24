package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.entity.ApiKey;
import com.autumn.loganalyzer_admin.model.TopicIndexDTO;
import com.autumn.loganalyzer_admin.repository.ApiKeyRepository;
import com.autumn.loganalyzer_admin.service.interfaces.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    @Override
    public Optional<TopicIndexDTO> getTopicIndexMapping(String topicName) {
        Optional<ApiKey> byKafkaTopic = apiKeyRepository.findByKafkaTopic(topicName);
        if (byKafkaTopic.isPresent()) {
            ApiKey apiKey = byKafkaTopic.get();
            return Optional.ofNullable(TopicIndexDTO.builder()
                    .elasticIndex(apiKey.getElasticIndex())
                    .kafkaTopic(apiKey.getKafkaTopic())
                    .build());
        }
        return Optional.empty();
    }
}
