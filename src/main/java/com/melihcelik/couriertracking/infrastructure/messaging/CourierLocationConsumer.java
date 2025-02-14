package com.melihcelik.couriertracking.infrastructure.messaging;

import com.melihcelik.couriertracking.application.command.CourierLocationCommandService;
import com.melihcelik.couriertracking.domain.event.CourierLocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourierLocationConsumer {
    private final CourierLocationCommandService commandService;

    @KafkaListener(
        topics = "courier.location",
        groupId = "courier-tracking-group",
        containerFactory = "courierLocationKafkaListenerContainerFactory"
    )
    public void consume(CourierLocationEvent event) {
        try {
            log.debug("Received courier location event: {}", event);
            commandService.processCourierLocation(event);
        } catch (Exception e) {
            log.error("Error processing courier location event: {}", event, e);
            // Don't rethrow the exception to prevent message redelivery
        }
    }
} 