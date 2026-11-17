package com.shipmonk.testingday.repo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shipmonk.testingday.entity.RateEntity;
import com.shipmonk.testingday.entity.RatesUpdateHistEntity;

public interface RateRepo extends JpaRepository<RateEntity, UUID> {

    List<RateEntity> findAllByHist(RatesUpdateHistEntity hist);

    List<RateEntity> findAllByHistAndCodeIn(RatesUpdateHistEntity hist, Collection<String> codes);

}
