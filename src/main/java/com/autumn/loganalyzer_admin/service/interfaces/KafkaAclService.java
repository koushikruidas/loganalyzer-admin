package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.KafkaAclRequest;

public interface KafkaAclService {
    void addAcl(KafkaAclRequest request);
}
