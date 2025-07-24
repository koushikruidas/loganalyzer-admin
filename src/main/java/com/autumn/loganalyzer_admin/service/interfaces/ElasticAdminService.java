package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.ElasticAdminDTO;

public interface ElasticAdminService {
    void createIndex(ElasticAdminDTO elasticAdminDTO);
    boolean indexExists(String indexName);
}
