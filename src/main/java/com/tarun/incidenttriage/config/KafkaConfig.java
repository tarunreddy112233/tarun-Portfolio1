package com.tarun.incidenttriage.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic incidentTopic() {
        return TopicBuilder.name("production-incidents")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
