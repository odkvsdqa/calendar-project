package com.calendar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "currency_code", length = 10, nullable = false, unique = true)
    private String currencyCode;
    
    @Column(name = "rate_to_twd", precision = 10, scale = 6, nullable = false)
    private BigDecimal rateToTwd;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    public CurrencyRate() {
        this.id = java.util.UUID.randomUUID().toString();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public CurrencyRate(String currencyCode, BigDecimal rateToTwd) {
        this();
        this.currencyCode = currencyCode;
        this.rateToTwd = rateToTwd;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public BigDecimal getRateToTwd() {
        return rateToTwd;
    }
    
    public void setRateToTwd(BigDecimal rateToTwd) {
        this.rateToTwd = rateToTwd;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}