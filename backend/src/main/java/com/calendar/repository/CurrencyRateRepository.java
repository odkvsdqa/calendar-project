package com.calendar.repository;

import com.calendar.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, String> {
    
    /**
     * 根據幣別代碼查詢匯率
     */
    Optional<CurrencyRate> findByCurrencyCode(String currencyCode);
    
    /**
     * 檢查幣別是否存在
     */
    boolean existsByCurrencyCode(String currencyCode);
}