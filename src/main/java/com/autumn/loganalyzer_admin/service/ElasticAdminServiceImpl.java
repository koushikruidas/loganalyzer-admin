package com.autumn.loganalyzer_admin.service;

import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import com.autumn.loganalyzer_admin.model.ElasticAdminDTO;
import com.autumn.loganalyzer_admin.service.interfaces.ElasticAdminService;
import org.elasticsearch.client.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

@Service
public class ElasticAdminServiceImpl implements ElasticAdminService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public void createIndex(ElasticAdminDTO elasticAdminDTO) {
        String indexName = elasticAdminDTO.getIndexName();
        if (!elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists()) {
            elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).create();
            System.out.println("Elasticsearch index created: " + indexName);
        } else {
            System.out.println("Elasticsearch index already exists: " + indexName);
        }
    }

    @Override
    public boolean indexExists(String indexName) {
        return elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();
    }
}
