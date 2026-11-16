package com.shipmonk.testingday.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shipmonk.testingday.entity.RatesProviderEntity;
import com.shipmonk.testingday.events.RatesProviderEvent;
import com.shipmonk.testingday.events.api.EventType;
import com.shipmonk.testingday.events.api.EventsPublisher;
import com.shipmonk.testingday.exceptions.MalformedRequestException;
import com.shipmonk.testingday.exceptions.ProviderRateNotFoundException;
import com.shipmonk.testingday.exceptions.RatesProvidersUnavailableException;
import com.shipmonk.testingday.model.ProviderResponse;
import com.shipmonk.testingday.providers.api.RatesProvider;
import com.shipmonk.testingday.repo.RatesProviderRepo;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("ratesProviderService")
public class RatesProviderService {

    private final RatesProviderRepo providerRepo;
    private final Map<String, RatesProvider> providers;
    private final EventsPublisher eventsPublisher;

    public RatesProviderService(
        RatesProviderRepo providerRepo,
        List<RatesProvider> providers,
        EventsPublisher eventsPublisher
    ) {
        this.providers = providers.stream()
            .collect(Collectors.toMap(RatesProvider::getName, Function.identity()));
        this.providerRepo = providerRepo;
        this.eventsPublisher = eventsPublisher;
    }

    @PostConstruct
    private void init() {
        if (providers.isEmpty()) {
            throw new RatesProvidersUnavailableException();
        }

        Set<String> existingProviders = providerRepo.findAllByNameIn(this.providers.keySet()).stream()
            .map(RatesProviderEntity::getName)
            .collect(Collectors.toSet());
        List<RatesProviderEntity> newProviders = providers.values().stream()
            .filter(provider -> !existingProviders.contains(provider.getName()))
            .map(provider -> RatesProviderEntity.builder()
                .name(provider.getName())
                .active(true)
                .priority(provider.getPriority())
                .build()
            )
            .toList();

        providerRepo.saveAll(newProviders);
        log.info("Current rates providers {}, new rates providers {}", providers, newProviders);
    }

    public List<ProviderResponse> getProviders() {
        return providerRepo.findAll().stream()
            .map(RatesProviderService::toResponse)
            .toList();
    }

    public RatesProviderEntity getProvider(String name) {
        return providerRepo.findByName(name)
            .orElseThrow(() -> new ProviderRateNotFoundException(name));
    }

    public List<RatesProvider> getActiveProviders() {
        return providerRepo.findAllActive(providers.keySet()).stream()
            .map(entity -> providers.get(entity.getName()))
            .toList();
    }

    @Transactional
    public ProviderResponse updateProvider(String name, boolean active) {
        RatesProviderEntity provider = providerRepo.findByName(name)
            .orElseThrow(() -> new ProviderRateNotFoundException(name));
        boolean updated = provider.getActive() != active;

        provider.setActive(active);

        if (updated) {
            log.info("Provider [{}] updated, [{}] -> [{}] ", name, !provider.getActive(), provider.getActive());
            eventsPublisher.publish(
                provider.getActive() ? EventType.PROVIDER_ENABLED : EventType.PROVIDER_DISABLED,
                RatesProviderEvent.builder()
                    .providerName(name)
                    .active(provider.getActive())
                    .build()
            );
        }

        return toResponse(provider);
    }

    @Transactional
    public ProviderResponse updatePriority(String name, int priority) {
        if (priority < 0) {
            throw new MalformedRequestException("Provider priority must not be negative, got [%d]".formatted(priority));
        }

        RatesProviderEntity provider = providerRepo.findByName(name)
            .orElseThrow(() -> new ProviderRateNotFoundException(name));

        log.info("Provider [{}] priority updated, [{}] -> [{}]", name, provider.getPriority(), priority);
        provider.setPriority(priority);

        return toResponse(provider);
    }

    private static ProviderResponse toResponse(RatesProviderEntity provider) {
        return ProviderResponse.builder()
            .name(provider.getName())
            .active(provider.getActive())
            .priority(provider.getPriority())
            .build();
    }

}
