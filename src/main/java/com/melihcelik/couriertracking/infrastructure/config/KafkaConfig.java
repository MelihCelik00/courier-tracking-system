package com.melihcelik.couriertracking.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

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

    // Producer Configuration
    @Bean
    public ProducerFactory<String, CourierLocationEvent> courierLocationProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new JsonSerializer<>(objectMapper()));
    }

    @Bean
    public ProducerFactory<String, StoreEntryEvent> storeEntryProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new JsonSerializer<>(objectMapper()));
    }

    @Bean
    public KafkaTemplate<String, CourierLocationEvent> courierLocationKafkaTemplate() {
        return new KafkaTemplate<>(courierLocationProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, StoreEntryEvent> storeEntryKafkaTemplate() {
        return new KafkaTemplate<>(storeEntryProducerFactory());
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, CourierLocationEvent> courierLocationConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "courier-tracking-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        JsonDeserializer<CourierLocationEvent> deserializer = new JsonDeserializer<>(CourierLocationEvent.class, objectMapper());
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.melihcelik.couriertracking.domain.event");
        
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, StoreEntryEvent> storeEntryConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "courier-tracking-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        JsonDeserializer<StoreEntryEvent> deserializer = new JsonDeserializer<>(StoreEntryEvent.class, objectMapper());
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.melihcelik.couriertracking.domain.event");
        
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CourierLocationEvent> courierLocationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CourierLocationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(courierLocationConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StoreEntryEvent> storeEntryKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StoreEntryEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(storeEntryConsumerFactory());
        return factory;
    }
} 