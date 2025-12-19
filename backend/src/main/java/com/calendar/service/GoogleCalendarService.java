package com.calendar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.calendar.dto.EventDTO;
import com.calendar.dto.google.GoogleCalendarResponse;
import com.calendar.dto.google.GoogleEventItem;

@Service
public class GoogleCalendarService {

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

	@Value("${google.api.key}")
	private String apiKey;

	private final RestTemplate restTemplate = new RestTemplate();

	// Google API 網址樣板
	private static final String API_URL = "https://www.googleapis.com/calendar/v3/calendars/{calendarId}/events?key={apiKey}&timeMin={timeMin}&timeMax={timeMax}&singleEvents=true&orderBy=startTime";

	public List<EventDTO> fetchHolidays(String calendarId, String countryIcon, String venueName) {
		List<EventDTO> events = new ArrayList<>();

		int year = LocalDate.now().getYear();
		String timeMin = year + "-01-01T00:00:00Z";
		String timeMax = year + "-12-31T23:59:59Z";

		log.info("🗓️ 正在透過 Google API 抓取: {}", venueName);

		try {
			// 🔥 關鍵修改：使用 Map 來存放參數，交給 RestTemplate 自動編碼
			Map<String, String> uriVariables = new HashMap<>();
			uriVariables.put("calendarId", calendarId); // 這裡放原始的 '#' 沒關係，它會幫你轉
			uriVariables.put("apiKey", apiKey);
			uriVariables.put("timeMin", timeMin);
			uriVariables.put("timeMax", timeMax);

			// 直接把 map 傳進去，讓它處理 URL
			GoogleCalendarResponse response = restTemplate.getForObject(API_URL, GoogleCalendarResponse.class,
					uriVariables);

			if (response != null && response.getItems() != null) {
				for (GoogleEventItem item : response.getItems()) {
					EventDTO dto = convertToDto(item, countryIcon);
					if (dto != null) {
						events.add(dto);
					}
				}
			}
			log.info("✅ {} 抓取完成，共 {} 筆", venueName, events.size());

		} catch (Exception e) {
			log.error("❌ Google API 呼叫失敗 ({}): {}", venueName, e.getMessage());
			// 印出更詳細的錯誤，方便除錯
			e.printStackTrace();
		}
		return events;
	}

	private EventDTO convertToDto(GoogleEventItem item, String icon) {
		try {
			EventDTO dto = new EventDTO();
			dto.setId(UUID.randomUUID().toString());
			dto.setTitle(icon + " " + item.getSummary());
			dto.setDescription("國定假日");
			dto.setColor("#dc2626"); // 紅色
			dto.setAllDay(true);

			// 處理日期：Google 假日通常給 "date" (YYYY-MM-DD)
			if (item.getStart().getDate() != null) {
				LocalDate date = LocalDate.parse(item.getStart().getDate());
				dto.setStartTime(LocalDateTime.of(date, LocalTime.MIN));
				dto.setEndTime(LocalDateTime.of(date, LocalTime.MAX));
			} else {
				// 如果是這種類型 (雖然假日很少見)，給一個預設處理
				return null;
			}
			return dto;
		} catch (Exception e) {
			return null;
		}
	}
}