package com.shipmonk.testingday.repo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RatesUpdateHistEntity;

public interface RatesUpdateHistRepo extends JpaRepository<RatesUpdateHistEntity, UUID> {

    @Query("FROM RateUpdateHistEntity WHERE success = true ORDER BY timestamp DESC LIMIT 1")
    Optional<RatesUpdateHistEntity> findLatestByTimestamp();

    @Query("FROM RateUpdateHistEntity WHERE success = true ORDER BY at DESC LIMIT 1")
    Optional<RatesUpdateHistEntity> findLatest();

    @Query("FROM RateUpdateHistEntity WHERE success = true AND at >= :start AND at < :end ORDER BY at DESC LIMIT 1")
    Optional<RatesUpdateHistEntity> findLatestBetween(LocalDateTime start, LocalDateTime end);

}
