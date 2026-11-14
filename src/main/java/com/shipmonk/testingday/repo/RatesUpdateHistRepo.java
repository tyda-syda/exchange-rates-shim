package com.shipmonk.testingday.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RatesUpdateHistEntity;

public interface RatesUpdateHistRepo extends JpaRepository<RatesUpdateHistEntity, UUID> {

    @Query("FROM RateUpdateHistEntity WHERE success = true ORDER BY timestamp DESC LIMIT 1")
    Optional<RatesUpdateHistEntity> findLatestByTimestamp();

    @Query("FROM RateUpdateHistEntity WHERE success = true ORDER BY at DESC LIMIT 1")
    Optional<RatesUpdateHistEntity> findLatestByAt();

}
