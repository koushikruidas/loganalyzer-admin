package com.autumn.loganalyzer_admin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;
    @Value("${spring.kafka.properties.sasl.mechanism}")
    private String saslMechanism;
    @Value("${spring.kafka.properties.sasl.jaas.config}")
    private String saslJaasConfig;

    // Kafka AdminClient bean
    @Bean
    public AdminClient adminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("security.protocol", securityProtocol);
        props.put("sasl.mechanism", saslMechanism);
        props.put("sasl.jaas.config", saslJaasConfig);

        props.put("ssl.keystore.location", System.getenv("SPRING_KAFKA_PROPERTIES_SSL_KEYSTORE_LOCATION"));
        props.put("ssl.keystore.password", System.getenv("SPRING_KAFKA_PROPERTIES_SSL_KEYSTORE_PASSWORD"));
        props.put("ssl.key.password", System.getenv("SPRING_KAFKA_PROPERTIES_SSL_KEY_PASSWORD"));
        
        return AdminClient.create(props);
    }
}
