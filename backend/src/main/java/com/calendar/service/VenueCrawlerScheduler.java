package com.calendar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.calendar.dto.EventDTO;
import com.calendar.model.ExternalEvent;
import com.calendar.repository.ExternalEventRepository;
import com.calendar.service.scraper.VenueScraper;

@Service
public class VenueCrawlerScheduler {

	private static final Logger log = LoggerFactory.getLogger(VenueCrawlerScheduler.class);

	@Autowired
	private List<VenueScraper> scrapers;

	@Autowired
	private ExternalEventRepository repository;

	// 🔥 修改點：使用 fixedRateString 讀取設定檔
	// 這樣以後要改時間，改 application.properties 就好
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRateString = "${app.crawler.fixed-rate}")
	public void runCrawlers() {
		log.info("⏰ 開始執行場館爬蟲排程...");

		for (VenueScraper scraper : scrapers) {
			try {
				processScraper(scraper);
			} catch (Exception e) {
				log.error("❌ 爬取 {} 失敗: {}", scraper.getVenueName(), e.getMessage());
			}
		}
		log.info("⏰ 所有場館爬蟲排程結束");
	}

	// 抽出邏輯讓代碼更乾淨
	private void processScraper(VenueScraper scraper) {
		String venueId = scraper.getVenueId();
		log.info("正在爬取: {}", scraper.getVenueName());

		List<EventDTO> rawDtos = scraper.scrapeEvents();
		if (rawDtos.isEmpty()) {
			return;
		}

		// 1. 清除舊資料
		repository.deleteByVenueId(venueId);
		repository.flush();

		// 2. 去重邏輯 (保持原本邏輯)
		Map<String, EventDTO> uniqueMap = new HashMap<>();
		for (EventDTO dto : rawDtos) {
			String cleanTitle = dto.getTitle().replace('\u00A0', ' ').trim();
			String key = cleanTitle + "|" + dto.getStartTime().toString();
			uniqueMap.put(key, dto);
		}

		List<EventDTO> uniqueDtos = new ArrayList<>(uniqueMap.values());
		log.info("原始抓取 {} 筆，去重後剩餘 {} 筆", rawDtos.size(), uniqueDtos.size());

		// 3. 寫入
		List<ExternalEvent> entities = uniqueDtos.stream().map(dto -> {
			ExternalEvent entity = new ExternalEvent();
			entity.setVenueId(venueId);
			entity.setTitle(dto.getTitle().replace('\u00A0', ' ').trim());
			entity.setDescription(dto.getDescription());
			entity.setStartTime(dto.getStartTime());
			entity.setEndTime(dto.getEndTime());
			return entity;
		}).collect(Collectors.toList());

		repository.saveAll(entities);
		log.info("✅ {} 更新完成", scraper.getVenueName());
	}
}