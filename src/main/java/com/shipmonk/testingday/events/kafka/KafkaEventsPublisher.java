package com.shipmonk.testingday.events.kafka;

import static com.shipmonk.testingday.events.api.EventType.PROVIDER_DISABLED;
import static com.shipmonk.testingday.events.api.EventType.PROVIDER_ENABLED;
import static com.shipmonk.testingday.events.api.EventType.RATES_PROVIDER_FAIL;

import java.time.LocalDateTime;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.shipmonk.testingday.events.api.Event;
import com.shipmonk.testingday.events.api.EventType;
import com.shipmonk.testingday.events.api.EventsPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "exchange.events", name = "type", havingValue = "kafka", matchIfMissing = true)
public class KafkaEventsPublisher implements EventsPublisher {

    private static final EnumSet<EventType> SUPPORTED_EVENTS = EnumSet.of(
        RATES_PROVIDER_FAIL,
        PROVIDER_DISABLED,
        PROVIDER_ENABLED
    );

    private final KafkaTemplate<String, String> producer;
    private final ObjectMapper objectMapper;

    @Value("${exchange.rates.events.kafka.rates-provider-topic}")
    private String ratesProviderTopic;

    @Override
    public void publish(EventType type, Object event) {
        if (!SUPPORTED_EVENTS.contains(type)) {
            log.info("Event of type [{}] is not supported, skipped", type);
            return;
        }

        Event wrappedEvent = Event.builder()
            .timestamp(LocalDateTime.now())
            .type(type)
            .payload(event)
            .build();

        try {
            producer.send(ratesProviderTopic, objectMapper.writeValueAsString(wrappedEvent));
            log.info("Published new event [{}]", wrappedEvent);
        } catch (Exception e) {
            log.error("Failed to publish event to [{}]: {}", ratesProviderTopic, event, e);
        }
    }

}
