package com.shipmonk.testingday.events.api;

public interface EventsPublisher {

    void publish(EventType type, Object event);

}
