package com.shipmonk.testingday.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RatesProviderEntity;

public interface RatesProviderRepo extends JpaRepository<RatesProviderEntity, UUID> {

    @Query("SELECT COUNT(*) FROM RatesProviderEntity WHERE name = :name AND active = true LIMIT 1")
    long countActive(String name);

    List<RatesProviderEntity> findAllByNameIn(List<String> names);

    Optional<RatesProviderEntity> findByName(String name);

}
