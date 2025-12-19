package com.calendar.service.scraper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendar.dto.EventDTO;
import com.calendar.service.GoogleCalendarService; // 引入剛剛寫的 Service

@Service
public class TaiwanHolidayScraper implements VenueScraper {

	@Autowired
	private GoogleCalendarService googleCalendarService;

	// Google 官方台灣假日 Calendar ID
	// 格式是：語言.國家#holiday@group.v.calendar.google.com
	private static final String CALENDAR_ID = "zh-tw.taiwan#holiday@group.v.calendar.google.com";

	@Override
	public String getVenueId() {
		return "holiday-tw";
	}

	@Override
	public String getVenueName() {
		return "台灣國定假日";
	}

	@Override
	public List<EventDTO> scrapeEvents() {
		// 呼叫 Service，傳入 ID 和 國旗圖示
		return googleCalendarService.fetchHolidays(CALENDAR_ID, "🇹🇼", getVenueName());
	}
}