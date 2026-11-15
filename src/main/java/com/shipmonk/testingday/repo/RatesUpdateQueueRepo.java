package com.shipmonk.testingday.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RatesUpdateQueueEntity;

public interface RatesUpdateQueueRepo extends JpaRepository<RatesUpdateQueueEntity, UUID> {

    @Modifying
    @Query("UPDATE RatesUpdateQueue SET done = true WHERE at = :at AND done = false")
    void markDone(LocalDate at);

    @Modifying
    @Query("UPDATE RatesUpdateQueue SET done = true WHERE :staledBefore >= timestamp AND done = false")
    int markDoneLongerThan(LocalDateTime staledBefore);

}
