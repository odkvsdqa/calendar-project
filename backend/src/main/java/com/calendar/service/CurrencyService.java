package com.calendar.service;

import com.calendar.model.CurrencyRate;
import com.calendar.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 幣別換算服務（支援自動更新）
 */
@Service
public class CurrencyService {
    
    @Autowired
    private CurrencyRateRepository rateRepository;
    
    @Autowired
    private ExchangeRateApiClient apiClient;
    
    /**
     * 應用啟動時檢查並更新匯率
     */
    @PostConstruct
    public void initRates() {
        try {
            // 檢查是否需要更新（超過 24 小時）
            if (shouldUpdateRates()) {
                System.out.println("🔄 匯率資料過舊，正在更新...");
                updateRatesFromApi();
            } else {
                System.out.println("✅ 匯率資料為最新，無需更新");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 匯率初始化失敗，使用資料庫現有資料: " + e.getMessage());
        }
    }
    
    /**
     * 每天凌晨 2:00 自動更新匯率
     * Cron: 秒 分 時 日 月 週
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledUpdateRates() {
        System.out.println("⏰ 排程更新匯率...");
        try {
            updateRatesFromApi();
        } catch (Exception e) {
            System.err.println("❌ 排程更新匯率失敗: " + e.getMessage());
        }
    }
    
    /**
     * 從 API 更新匯率到資料庫
     */
    @Transactional
    public void updateRatesFromApi() {
        try {
            Map<String, BigDecimal> latestRates = apiClient.fetchLatestRates();
            
            for (Map.Entry<String, BigDecimal> entry : latestRates.entrySet()) {
                String currency = entry.getKey();
                BigDecimal rate = entry.getValue();
                
                CurrencyRate rateEntity = rateRepository.findByCurrencyCode(currency)
                        .orElse(new CurrencyRate(currency, rate));
                
                rateEntity.setRateToTwd(rate);
                rateEntity.setLastUpdated(LocalDateTime.now());
                
                rateRepository.save(rateEntity);
            }
            
            System.out.println("✅ 匯率更新成功: " + LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("❌ 匯率更新失敗: " + e.getMessage());
            throw new RuntimeException("匯率更新失敗", e);
        }
    }
    
    /**
     * 檢查是否需要更新匯率（超過 24 小時）
     */
    private boolean shouldUpdateRates() {
        return rateRepository.findByCurrencyCode("USD")
                .map(rate -> {
                    LocalDateTime lastUpdate = rate.getLastUpdated();
                    LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
                    return lastUpdate.isBefore(dayAgo);
                })
                .orElse(true); // 如果沒有資料，需要更新
    }
    
    /**
     * 將任意幣別換算成 TWD（從資料庫讀取匯率）
     */
    public BigDecimal convertToTWD(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = rateRepository.findByCurrencyCode(currency)
                .map(CurrencyRate::getRateToTwd)
                .orElse(BigDecimal.ONE); // Fallback
        
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 批次換算（用於統計）
     */
    public BigDecimal convertAllToTWD(Map<String, BigDecimal> costs) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (Map.Entry<String, BigDecimal> entry : costs.entrySet()) {
            String currency = entry.getKey();
            BigDecimal amount = entry.getValue();
            total = total.add(convertToTWD(amount, currency));
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 取得當前匯率表（供前端顯示）
     */
    public Map<String, BigDecimal> getCurrentRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        rateRepository.findAll().forEach(rate -> {
            rates.put(rate.getCurrencyCode(), rate.getRateToTwd());
        });
        
        return rates;
    }
    
    /**
     * 取得最後更新時間
     */
    public LocalDateTime getLastUpdateTime() {
        return rateRepository.findByCurrencyCode("USD")
                .map(CurrencyRate::getLastUpdated)
                .orElse(null);
    }
}