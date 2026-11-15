package com.shipmonk.testingday.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "RateUpdateHistEntity")
@Table(name = "rate_update_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatesUpdateHistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RatesProviderEntity provider;

    @Column(nullable = false)
    private LocalDateTime at;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 3)
    private String base;

    @Column(columnDefinition = "TEXT")
    private String message;

}
