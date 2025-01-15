package com.melihcelik.couriertracking.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;
    private final String bootstrapServers = "localhost:9092";

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServers);
    }

    @Test
    void objectMapper_ShouldBeConfiguredWithJavaTimeModule() {
        // Act
        ObjectMapper mapper = kafkaConfig.objectMapper();

        // Assert
        assertTrue(mapper.findModules().stream()
                .anyMatch(module -> module.getClass().getSimpleName().contains("JavaTimeModule")));
    }

    @Test
    void courierLocationTopic_ShouldBeConfiguredCorrectly() {
        // Act
        NewTopic topic = kafkaConfig.courierLocationTopic();

        // Assert
        assertEquals("courier.location", topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals(1, (short) topic.replicationFactor());
    }

    @Test
    void storeEntryTopic_ShouldBeConfiguredCorrectly() {
        // Act
        NewTopic topic = kafkaConfig.storeEntryTopic();

        // Assert
        assertEquals("store.entry", topic.name());
        assertEquals(3, topic.numPartitions());
        assertEquals(1, (short) topic.replicationFactor());
    }

    @Test
    void courierLocationProducerFactory_ShouldBeConfiguredCorrectly() {
        // Act
        ProducerFactory<String, CourierLocationEvent> factory = kafkaConfig.courierLocationProducerFactory();

        // Assert
        assertTrue(factory instanceof DefaultKafkaProducerFactory);
        var configs = ((DefaultKafkaProducerFactory<?, ?>) factory).getConfigurationProperties();
        assertEquals(bootstrapServers, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void storeEntryProducerFactory_ShouldBeConfiguredCorrectly() {
        // Act
        ProducerFactory<String, StoreEntryEvent> factory = kafkaConfig.storeEntryProducerFactory();

        // Assert
        assertTrue(factory instanceof DefaultKafkaProducerFactory);
        var configs = ((DefaultKafkaProducerFactory<?, ?>) factory).getConfigurationProperties();
        assertEquals(bootstrapServers, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void courierLocationConsumerFactory_ShouldBeConfiguredCorrectly() {
        // Act
        ConsumerFactory<String, CourierLocationEvent> factory = kafkaConfig.courierLocationConsumerFactory();

        // Assert
        assertTrue(factory instanceof DefaultKafkaConsumerFactory);
        var configs = ((DefaultKafkaConsumerFactory<?, ?>) factory).getConfigurationProperties();
        assertEquals(bootstrapServers, configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("courier-tracking-group", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", configs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        
        // Check deserializer instances
        DefaultKafkaConsumerFactory<String, CourierLocationEvent> consumerFactory = (DefaultKafkaConsumerFactory<String, CourierLocationEvent>) factory;
        assertTrue(consumerFactory.getKeyDeserializer() instanceof StringDeserializer);
        assertTrue(consumerFactory.getValueDeserializer() instanceof JsonDeserializer);
    }

    @Test
    void storeEntryConsumerFactory_ShouldBeConfiguredCorrectly() {
        // Act
        ConsumerFactory<String, StoreEntryEvent> factory = kafkaConfig.storeEntryConsumerFactory();

        // Assert
        assertTrue(factory instanceof DefaultKafkaConsumerFactory);
        var configs = ((DefaultKafkaConsumerFactory<?, ?>) factory).getConfigurationProperties();
        assertEquals(bootstrapServers, configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("courier-tracking-group", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", configs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        
        // Check deserializer instances
        DefaultKafkaConsumerFactory<String, StoreEntryEvent> consumerFactory = (DefaultKafkaConsumerFactory<String, StoreEntryEvent>) factory;
        assertTrue(consumerFactory.getKeyDeserializer() instanceof StringDeserializer);
        assertTrue(consumerFactory.getValueDeserializer() instanceof JsonDeserializer);
    }

    @Test
    void kafkaTemplates_ShouldBeCreatedWithCorrectFactories() {
        // Act
        KafkaTemplate<String, CourierLocationEvent> courierTemplate = kafkaConfig.courierLocationKafkaTemplate();
        KafkaTemplate<String, StoreEntryEvent> storeTemplate = kafkaConfig.storeEntryKafkaTemplate();

        // Assert
        assertNotNull(courierTemplate);
        assertNotNull(storeTemplate);
        assertTrue(courierTemplate.getProducerFactory() instanceof DefaultKafkaProducerFactory);
        assertTrue(storeTemplate.getProducerFactory() instanceof DefaultKafkaProducerFactory);
    }
} 