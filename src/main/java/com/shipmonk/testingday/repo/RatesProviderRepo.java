package com.shipmonk.testingday.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shipmonk.testingday.entity.RatesProviderEntity;

public interface RatesProviderRepo extends JpaRepository<RatesProviderEntity, UUID> {

    @Query("FROM RatesProviderEntity WHERE active = true AND name IN (:names) ORDER BY priority ASC, name ASC")
    List<RatesProviderEntity> findAllActive(Collection<String> names);

    List<RatesProviderEntity> findAllByNameIn(Collection<String> names);

    Optional<RatesProviderEntity> findByName(String name);

}
