package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.TopicIndexDTO;

import java.util.Optional;

public interface ApiKeyService {
    Optional<TopicIndexDTO> getTopicIndexMapping(String topicName);
}
