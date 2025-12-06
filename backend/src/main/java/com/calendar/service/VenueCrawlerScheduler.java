package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.model.ExternalEvent;
import com.calendar.repository.ExternalEventRepository;
import com.calendar.service.scraper.VenueScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VenueCrawlerScheduler {

    private static final Logger log = LoggerFactory.getLogger(VenueCrawlerScheduler.class);

    @Autowired
    private List<VenueScraper> scrapers;

    @Autowired
    private ExternalEventRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedRate = 10800000) 
    @Transactional
    public void runCrawlers() {
        log.info("⏰ 開始執行場館爬蟲排程...");

        for (VenueScraper scraper : scrapers) {
            try {
                String venueId = scraper.getVenueId();
                log.info("正在爬取: {}", scraper.getVenueName());

                List<EventDTO> rawDtos = scraper.scrapeEvents();

                if (rawDtos.isEmpty()) {
                    continue;
                }

                // 1. 先清除資料庫舊資料
                repository.deleteByVenueId(venueId);
                repository.flush(); 

                // 2. 🔥【超級守門員】暴力去重邏輯
                // 使用 Map 來過濾，Key 相同就會直接覆蓋，絕對不會有兩筆
                Map<String, EventDTO> uniqueMap = new HashMap<>();
                
                for (EventDTO dto : rawDtos) {
                    // 建立唯一指紋：標題 + 開始時間
                    // .trim() 去除前後空白
                    // .replace 處理網頁常見的 "不換行空格" (Non-breaking space)
                    String cleanTitle = dto.getTitle().replace('\u00A0', ' ').trim();
                    String key = cleanTitle + "|" + dto.getStartTime().toString();
                    
                    uniqueMap.put(key, dto);
                }
                
                // 從 Map 取出乾淨的 Values
                List<EventDTO> uniqueDtos = new ArrayList<>(uniqueMap.values());
                
                log.info("原始抓取 {} 筆，去重後剩餘 {} 筆", rawDtos.size(), uniqueDtos.size());

                // 3. 寫入資料庫
                List<ExternalEvent> entities = uniqueDtos.stream().map(dto -> {
                    ExternalEvent entity = new ExternalEvent();
                    entity.setVenueId(venueId);
                    entity.setTitle(dto.getTitle().replace('\u00A0', ' ').trim()); // 再次確保乾淨
                    entity.setDescription(dto.getDescription());
                    entity.setStartTime(dto.getStartTime());
                    entity.setEndTime(dto.getEndTime());
                    return entity;
                }).collect(Collectors.toList());

                repository.saveAll(entities);
                log.info("✅ {} 更新完成，已寫入 {} 筆資料", scraper.getVenueName(), entities.size());

            } catch (Exception e) {
                log.error("❌ 爬取 {} 失敗: {}", scraper.getVenueName(), e.getMessage());
            }
        }
        log.info("⏰ 所有場館爬蟲排程結束");
    }
}