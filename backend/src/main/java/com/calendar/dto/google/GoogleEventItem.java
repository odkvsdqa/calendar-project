package com.calendar.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleEventItem {
	private String summary; // 活動標題
	private GoogleDate start;
	private GoogleDate end;

	// Getters & Setters
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public GoogleDate getStart() {
		return start;
	}

	public void setStart(GoogleDate start) {
		this.start = start;
	}

	public GoogleDate getEnd() {
		return end;
	}

	public void setEnd(GoogleDate end) {
		this.end = end;
	}

	// 內部類別：處理 Google 的日期格式
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GoogleDate {
		private String date; // 格式: "2025-01-01" (全天事件用這個)
		private String dateTime; // 格式: ISO時間 (非全天用這個)

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getDateTime() {
			return dateTime;
		}

		public void setDateTime(String dateTime) {
			this.dateTime = dateTime;
		}
	}
}