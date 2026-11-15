package com.shipmonk.testingday.repo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RateEntity;

public interface RateRepo extends JpaRepository<RateEntity, UUID> {

    @Query("FROM RateEntity ORDER BY at DESC LIMIT 1")
    Optional<RateEntity> findLatest();

    List<RateEntity> findAllByAt(LocalDateTime at);

    @Query("FROM RateEntity WHERE at = :at AND code IN (:codes)")
    List<RateEntity> findAllByAt(LocalDateTime at, Collection<String> codes);

    @Query("FROM RateEntity WHERE at >= :start AND at < :end ORDER BY at DESC LIMIT 1")
    Optional<RateEntity> findLatestBetween(LocalDateTime start, LocalDateTime end);

}
