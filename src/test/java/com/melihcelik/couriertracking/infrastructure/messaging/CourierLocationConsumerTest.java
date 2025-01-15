package com.melihcelik.couriertracking.infrastructure.messaging;

import com.melihcelik.couriertracking.application.command.CourierLocationCommandService;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierLocationConsumerTest {

    @Mock
    private CourierLocationCommandService commandService;

    private CourierLocationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new CourierLocationConsumer(commandService);
    }

    @Test
    void consume_ShouldProcessCourierLocationEvent() {
        // Arrange
        CourierLocationEvent event = CourierLocationEvent.builder()
                .courierId(1L)
                .latitude(40.9923307)
                .longitude(29.1244229)
                .build();

        // Act
        consumer.consume(event);

        // Assert
        verify(commandService).processCourierLocation(event);
    }

    @Test
    void consume_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        CourierLocationEvent event = CourierLocationEvent.builder()
                .courierId(1L)
                .latitude(40.9923307)
                .longitude(29.1244229)
                .build();
        doThrow(new RuntimeException("Test exception"))
                .when(commandService).processCourierLocation(event);

        // Act & Assert
        consumer.consume(event); // Should not throw exception
        verify(commandService).processCourierLocation(event);
    }
} 