package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.KafkaAdminDTO;

public interface KafkaAdminService {
    void createTopic(KafkaAdminDTO kafkaAdminDTO);
}
