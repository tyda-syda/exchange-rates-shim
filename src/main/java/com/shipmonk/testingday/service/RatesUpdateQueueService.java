package com.shipmonk.testingday.service;

import static com.shipmonk.testingday.utils.ExceptionUtils.isUniqueViolation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.shipmonk.testingday.entity.RatesUpdateQueueEntity;
import com.shipmonk.testingday.events.RatesUpdateDoneEvent;
import com.shipmonk.testingday.exceptions.RatesUpdateInProgressException;
import com.shipmonk.testingday.repo.RatesUpdateQueueRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatesUpdateQueueService {

    private final RatesUpdateQueueRepo queueRepo;

    @Value("${exchange.rates.update-queue.max-queue-lease-sec:30}")
    private int maxQueueLease;

    public void enterQueue(LocalDate at) {
        try {
            RatesUpdateQueueEntity queueEntity = queueRepo.save(
                RatesUpdateQueueEntity.builder()
                    .timestamp(LocalDateTime.now())
                    .done(false)
                    .at(at)
                    .build()
            );
            log.info("Rates update queue new entity [{}]", queueEntity);
        } catch (DataIntegrityViolationException e) {
            if (!isUniqueViolation(e)) {
                throw e;
            }

            throw new RatesUpdateInProgressException(at);
        }
    }

    @Transactional
    public void leaveQueue(LocalDate at) {
        queueRepo.markDone(at);
    }

    @Transactional
    public void cleanupStaleEntries() {
        int staleCount = queueRepo.markDoneLongerThan(LocalDateTime.now().minusSeconds(maxQueueLease));

        if (staleCount > 0) {
            log.warn("Rates update queue had [{}] stale entries", staleCount);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void eventListener(RatesUpdateDoneEvent event) {
        log.info("Rates update done for [{}]", event.getAt());
        queueRepo.markDone(event.getAt());
    }

}
