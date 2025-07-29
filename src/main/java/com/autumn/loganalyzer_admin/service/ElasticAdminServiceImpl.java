package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.ElasticAdminDTO;
import com.autumn.loganalyzer_admin.service.interfaces.ElasticAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticAdminServiceImpl implements ElasticAdminService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private RestTemplate restTemplate;

    @Value("${elastic.base-url}")
    private String elasticBaseUrl;

    @Value("${elastic.auth.username}")
    private String username;

    @Value("${elastic.auth.password}")
    private String password;

    @Value("${elastic.ilm.policy.name}")
    private String ilmPolicyName;

    @Value("${elastic.index.template.shards}")
    private int numberOfShards;

    @Value("${elastic.index.template.replicas}")
    private int numberOfReplicas;

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

//    @Override
    public void createIndexWithILM(String indexName, String aliasName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(username, password);

            // 1. Create ILM Policy
            String ilmUrl = elasticBaseUrl + "/_ilm/policy/" + ilmPolicyName;
            Map<String, Object> ilmPolicy = new HashMap<>();
            ilmPolicy.put("policy", Map.of(
                    "phases", Map.of(
                            "hot", Map.of("actions", Map.of("rollover", Map.of("max_size", "50gb", "max_age", "30d"))),
                            "delete", Map.of("min_age", "90d", "actions", Map.of("delete", Map.of()))
                    )
            ));
            HttpEntity<Map<String, Object>> ilmEntity = new HttpEntity<>(ilmPolicy, headers);
            restTemplate.put(ilmUrl, ilmEntity);
            System.out.println("ILM policy created: " + ilmPolicyName);

            // 2. Create Index Template
            String templateUrl = elasticBaseUrl + "/_index_template/" + aliasName + "-template";
            Map<String, Object> templateBody = Map.of(
                    "index_patterns", List.of(aliasName + "-*"),
                    "template", Map.of(
                            "settings", Map.of(
                                    "number_of_shards", numberOfShards,
                                    "number_of_replicas", numberOfReplicas,
                                    "index.lifecycle.name", ilmPolicyName,
                                    "index.lifecycle.rollover_alias", aliasName
                            ),
                            "aliases", Map.of(aliasName, Map.of("is_write_index", true))
                    )
            );
            HttpEntity<Map<String, Object>> templateEntity = new HttpEntity<>(templateBody, headers);
            restTemplate.put(templateUrl, templateEntity);
            System.out.println("Index template created with ILM and alias: " + aliasName);

            // 3. Create Initial Index
            String initialIndexName = aliasName + "-000001";
            String createIndexUrl = elasticBaseUrl + "/" + initialIndexName;
            HttpEntity<String> indexEntity = new HttpEntity<>("{}", headers);
            restTemplate.put(createIndexUrl, indexEntity);
            System.out.println("Initial index created: " + initialIndexName);

        } catch (Exception e) {
            System.err.println("Error creating index with ILM: " + e.getMessage());
        }
    }
}
