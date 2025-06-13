package com.kleberrhuan.houer.common.application.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher 
        implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;
    
    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }
    
    @Override
    public <T> void publish(T event) {
        springPublisher.publishEvent(event);
    }
}
