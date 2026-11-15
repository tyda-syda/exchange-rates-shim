package com.shipmonk.testingday.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shipmonk.testingday.entity.RatesProviderEntity;
import com.shipmonk.testingday.events.RatesProviderEvent;
import com.shipmonk.testingday.events.api.EventType;
import com.shipmonk.testingday.events.api.EventsPublisher;
import com.shipmonk.testingday.exceptions.ProviderRateNotFoundException;
import com.shipmonk.testingday.model.ProviderResponse;
import com.shipmonk.testingday.providers.api.RatesProvider;
import com.shipmonk.testingday.repo.RatesProviderRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("ratesProviderService")
@RequiredArgsConstructor
public class RatesProviderService {

    private final RatesProviderRepo providerRepo;
    private final List<RatesProvider> providers;
    private final EventsPublisher eventsPublisher;

    @PostConstruct
    private void init() {
        List<String> providerNames = providers.stream().map(RatesProvider::getName).toList();
        Set<String> existingProviders = providerRepo.findAllByNameIn(providerNames).stream()
            .map(RatesProviderEntity::getName)
            .collect(Collectors.toSet());
        List<RatesProviderEntity> newProviders = providers.stream()
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
            .map(entity ->
                ProviderResponse.builder()
                    .name(entity.getName())
                    .priority(entity.getPriority())
                    .active(entity.getActive())
                    .build()
            )
            .toList();
    }

    public RatesProviderEntity getProviderEntity(String name) {
        return providerRepo.findByName(name)
            .orElseThrow(() -> new ProviderRateNotFoundException(name));
    }

    public List<RatesProvider> getActiveProviders() {
        return providers.stream()
            .filter(provider -> providerRepo.countActive(provider.getName()) > 0)
            .sorted(Comparator.comparingInt(RatesProvider::getPriority))
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

        return ProviderResponse.builder()
            .name(name)
            .active(provider.getActive())
            .priority(provider.getPriority())
            .build();
    }

}
