package com.melihcelik.couriertracking.infrastructure.messaging;

import com.melihcelik.couriertracking.application.command.StoreEntryCommandService;
import com.melihcelik.couriertracking.domain.event.StoreEntryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEntryConsumer {
    private final StoreEntryCommandService commandService;

    @KafkaListener(
        topics = "store.entry",
        groupId = "courier-tracking-group",
        containerFactory = "storeEntryKafkaListenerContainerFactory"
    )
    public void consume(StoreEntryEvent event) {
        try {
            log.debug("Received store entry event: {}", event);
            commandService.processStoreEntry(event);
        } catch (Exception e) {
            log.error("Error processing store entry event: {}", event, e);
            // Don't rethrow the exception to prevent message redelivery
        }
    }
} 