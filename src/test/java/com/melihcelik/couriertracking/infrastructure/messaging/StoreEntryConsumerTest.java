package com.melihcelik.couriertracking.infrastructure.messaging;

import com.melihcelik.couriertracking.application.command.StoreEntryCommandService;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreEntryConsumerTest {

    @Mock
    private StoreEntryCommandService commandService;

    private StoreEntryConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new StoreEntryConsumer(commandService);
    }

    @Test
    void consume_ShouldProcessStoreEntryEvent() {
        // Arrange
        StoreEntryEvent event = StoreEntryEvent.builder()
                .courierId(1L)
                .storeId(1L)
                .storeName("Test Store")
                .latitude(40.9923307)
                .longitude(29.1244229)
                .timestamp(Instant.now())
                .build();

        // Act
        consumer.consume(event);

        // Assert
        verify(commandService).processStoreEntry(event);
    }

    @Test
    void consume_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        StoreEntryEvent event = StoreEntryEvent.builder()
                .courierId(1L)
                .storeId(1L)
                .storeName("Test Store")
                .latitude(40.9923307)
                .longitude(29.1244229)
                .timestamp(Instant.now())
                .build();
        doThrow(new RuntimeException("Test exception"))
                .when(commandService).processStoreEntry(event);

        // Act & Assert
        consumer.consume(event); // Should not throw exception
        verify(commandService).processStoreEntry(event);
    }
} 