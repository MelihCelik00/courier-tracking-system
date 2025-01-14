package com.melihcelik.couriertracking.infrastructure.config;

import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic courierLocationTopic() {
        return TopicBuilder.name("courier.location")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic storeEntryTopic() {
        return TopicBuilder.name("store.entry")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, CourierLocationEvent> courierLocationKafkaTemplate(
            ProducerFactory<String, CourierLocationEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, StoreEntryEvent> storeEntryKafkaTemplate(
            ProducerFactory<String, StoreEntryEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
} 