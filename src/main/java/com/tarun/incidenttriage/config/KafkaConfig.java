package com.tarun.incidenttriage.config;

import com.tarun.incidenttriage.incident.IncidentEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic incidentTopic() {
        return TopicBuilder.name("production-incidents")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, IncidentEvent> incidentProducerFactory() {
        Map<String, Object> props = Map.of(
                "bootstrap.servers", "${spring.kafka.bootstrap-servers:localhost:9092}"
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, IncidentEvent> incidentConsumerFactory() {
        Map<String, Object> props = Map.of(
                "bootstrap.servers", "${spring.kafka.bootstrap-servers:localhost:9092}"
        );
        JsonDeserializer<IncidentEvent> deserializer = new JsonDeserializer<>(IncidentEvent.class, JacksonUtils.enhancedObjectMapper());
        deserializer.addTrustedPackages("com.tarun.incidenttriage.incident");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), deserializer);
    }

    @Bean
    public KafkaTemplate<String, IncidentEvent> incidentKafkaTemplate(ProducerFactory<String, IncidentEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
