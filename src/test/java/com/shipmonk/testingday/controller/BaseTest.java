package com.shipmonk.testingday.controller;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.shipmonk.testingday.entity.RatesProviderEntity;
import com.shipmonk.testingday.entity.RatesUpdateHistEntity;
import com.shipmonk.testingday.events.api.EventsPublisher;
import com.shipmonk.testingday.providers.fixer.FixerProvider;
import com.shipmonk.testingday.repo.RateRepo;
import com.shipmonk.testingday.repo.RatesProviderRepo;
import com.shipmonk.testingday.repo.RatesUpdateHistRepo;
import com.shipmonk.testingday.repo.RatesUpdateQueueRepo;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
    "spring.quartz.auto-startup=false"
})
@Import(BaseTest.HistoryRepoConfig.class)
abstract class BaseTest {

    static final String PROVIDER_NAME = "fixer.io";

    @MockitoBean
    RateRepo rateRepo;

    @Autowired
    RatesUpdateHistRepo historyRepo;

    @MockitoBean
    RatesProviderRepo providerRepo;

    @MockitoBean
    RatesUpdateQueueRepo queueRepo;

    @MockitoBean
    FixerProvider fixerProvider;

    @MockitoBean
    EventsPublisher eventsPublisher;

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUpBase() {
        mockMvc = webAppContextSetup(context).build();
        clearInvocations(historyRepo);

        when(fixerProvider.getName()).thenReturn(PROVIDER_NAME);
        when(providerRepo.countActive(PROVIDER_NAME)).thenReturn(1L);
        when(providerRepo.findByName(PROVIDER_NAME)).thenReturn(Optional.of(provider(true)));
    }

    @TestConfiguration
    static class HistoryRepoConfig {

        @Bean
        RatesUpdateHistRepo historyRepo() {
            RatesUpdateHistRepo repo = mock(RatesUpdateHistRepo.class);
            RatesUpdateHistEntity lastUpdate = RatesUpdateHistEntity.builder()
                .success(true)
                .at(LocalDateTime.now())
                .timestamp(LocalDateTime.now())
                .build();

            when(repo.findLatestByTimestamp()).thenReturn(Optional.of(lastUpdate));
            return repo;
        }

    }

    static RatesProviderEntity provider(boolean active) {
        return RatesProviderEntity.builder()
            .name(PROVIDER_NAME)
            .active(active)
            .priority(1)
            .build();
    }

}
