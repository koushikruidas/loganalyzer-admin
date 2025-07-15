package com.autumn.loganalyzer_admin.healthcheck;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchHealthChecker.class);

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchHealthChecker(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PostConstruct
    public void checkElasticsearchConnection() {
        try {
            boolean available = elasticsearchClient.ping().value();
            if (available) {
                logger.info("Elasticsearch is reachable");
            } else {
                logger.warn("Elasticsearch ping failed");
            }
        } catch (Exception e) {
            logger.error("Failed to connect to Elasticsearch", e);
        }
    }
}
