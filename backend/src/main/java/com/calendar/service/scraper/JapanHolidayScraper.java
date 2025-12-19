package com.calendar.service.scraper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendar.dto.EventDTO;
import com.calendar.service.GoogleCalendarService;

@Service
public class JapanHolidayScraper implements VenueScraper {

	@Autowired
	private GoogleCalendarService googleCalendarService;

	// Google 官方日本假日 Calendar ID
	private static final String CALENDAR_ID = "ja.japanese#holiday@group.v.calendar.google.com";

	@Override
	public String getVenueId() {
		return "holiday-jp";
	}

	@Override
	public String getVenueName() {
		return "日本國定假日";
	}

	@Override
	public List<EventDTO> scrapeEvents() {
		return googleCalendarService.fetchHolidays(CALENDAR_ID, "🇯🇵", getVenueName());
	}
}