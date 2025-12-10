package com.calendar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; // 🔥 標準 Log
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // 🔥 注入配置
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExchangeRateApiClient {
    
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateApiClient.class);
    
    // 🔥 從 application.properties 讀取 URL
    @Value("${app.exchange-rate.api-url}")
    private String apiUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public ExchangeRateApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, BigDecimal> fetchLatestRates() throws IOException, InterruptedException {
        log.info("🌐 正在從 API 獲取最新匯率: {}", apiUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            log.error("API 請求失敗，狀態碼: {}", response.statusCode());
            throw new IOException("API 請求失敗: " + response.statusCode());
        }
        
        return parseRatesResponse(response.body());
    }
    
    private Map<String, BigDecimal> parseRatesResponse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode rates = root.get("rates");
        
        BigDecimal usdToTwd = new BigDecimal(rates.get("TWD").asText());
        Map<String, BigDecimal> result = new HashMap<>();
        
        result.put("TWD", BigDecimal.ONE);
        result.put("USD", usdToTwd.setScale(6, RoundingMode.HALF_UP));
        
        // Helper function to cleaner code
        addRate(result, rates, "JPY", usdToTwd);
        addRate(result, rates, "EUR", usdToTwd);
        addRate(result, rates, "CNY", usdToTwd);
        addRate(result, rates, "KRW", usdToTwd);
        
        log.info("✅ 匯率獲取成功，共 {} 種幣別", result.size());
        return result;
    }

    private void addRate(Map<String, BigDecimal> result, JsonNode rates, String currency, BigDecimal usdToTwd) {
        if (rates.has(currency)) {
            BigDecimal usdToTarget = new BigDecimal(rates.get(currency).asText());
            // Target -> TWD = (USD->TWD) / (USD->Target)
            BigDecimal targetToTwd = usdToTwd.divide(usdToTarget, 6, RoundingMode.HALF_UP);
            result.put(currency, targetToTwd);
        }
    }
}