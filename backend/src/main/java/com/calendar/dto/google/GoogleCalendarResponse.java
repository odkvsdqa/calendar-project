package com.calendar.dto.google;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleCalendarResponse {
	private List<GoogleEventItem> items;

	public List<GoogleEventItem> getItems() {
		return items;
	}

	public void setItems(List<GoogleEventItem> items) {
		this.items = items;
	}
}