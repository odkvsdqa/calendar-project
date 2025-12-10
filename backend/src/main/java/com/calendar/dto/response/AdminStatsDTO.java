package com.calendar.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminStatsDTO {

	// 1. 用於 "特定日期查詢" (/users-by-date)
	public static class DailyStats {
		private LocalDate date;
		private int userCount;
		private int eventCount;
		private List<AdminUserDTO> users;

		public DailyStats(LocalDate date, int userCount, int eventCount, List<AdminUserDTO> users) {
			this.date = date;
			this.userCount = userCount;
			this.eventCount = eventCount;
			this.users = users;
		}

		// Getters
		public LocalDate getDate() {
			return date;
		}

		public int getUserCount() {
			return userCount;
		}

		public int getEventCount() {
			return eventCount;
		}

		public List<AdminUserDTO> getUsers() {
			return users;
		}
	}

	// 2. 用於 "月報表" (/monthly-stats)
	public static class MonthlyStats {
		private int year;
		private int month;
		private Long totalUsers;
		private int totalEvents;
		private BigDecimal totalCost;
		private Map<String, Integer> dailyUserCount;

		public MonthlyStats(int year, int month, Long totalUsers, int totalEvents, BigDecimal totalCost,
				Map<String, Integer> dailyUserCount) {
			this.year = year;
			this.month = month;
			this.totalUsers = totalUsers;
			this.totalEvents = totalEvents;
			this.totalCost = totalCost;
			this.dailyUserCount = dailyUserCount;
		}

		// Getters
		public int getYear() {
			return year;
		}

		public int getMonth() {
			return month;
		}

		public Long getTotalUsers() {
			return totalUsers;
		}

		public int getTotalEvents() {
			return totalEvents;
		}

		public BigDecimal getTotalCost() {
			return totalCost;
		}

		public Map<String, Integer> getDailyUserCount() {
			return dailyUserCount;
		}
	}

	// 3. 用於 "時間範圍報表" (/time-range-stats)
	public static class TimeRangeStats {
		private LocalDateTime startTime;
		private LocalDateTime endTime;
		private Long totalUsers;
		private int totalEvents;
		private BigDecimal totalCost;
		private Map<String, Integer> hourlyUserCount;
		private List<AdminUserDTO> users;

		public TimeRangeStats(LocalDateTime startTime, LocalDateTime endTime, Long totalUsers, int totalEvents,
				BigDecimal totalCost, Map<String, Integer> hourlyUserCount, List<AdminUserDTO> users) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.totalUsers = totalUsers;
			this.totalEvents = totalEvents;
			this.totalCost = totalCost;
			this.hourlyUserCount = hourlyUserCount;
			this.users = users;
		}

		// Getters
		public LocalDateTime getStartTime() {
			return startTime;
		}

		public LocalDateTime getEndTime() {
			return endTime;
		}

		public Long getTotalUsers() {
			return totalUsers;
		}

		public int getTotalEvents() {
			return totalEvents;
		}

		public BigDecimal getTotalCost() {
			return totalCost;
		}

		public Map<String, Integer> getHourlyUserCount() {
			return hourlyUserCount;
		}

		public List<AdminUserDTO> getUsers() {
			return users;
		}
	}

	// 4. 用於 "用戶排程統計" (/user-schedule-dates)
	public static class UserScheduleStats {
		private String userId;
		private String username;
		private int totalScheduleDays;
		private List<String> scheduleDates;

		public UserScheduleStats(String userId, String username, int totalScheduleDays, List<String> scheduleDates) {
			this.userId = userId;
			this.username = username;
			this.totalScheduleDays = totalScheduleDays;
			this.scheduleDates = scheduleDates;
		}

		// Getters
		public String getUserId() {
			return userId;
		}

		public String getUsername() {
			return username;
		}

		public int getTotalScheduleDays() {
			return totalScheduleDays;
		}

		public List<String> getScheduleDates() {
			return scheduleDates;
		}
	}

	// 5. 用於 "匯率看板" (/exchange-rates)
	public static class ExchangeRateDashboard {
		private Map<String, BigDecimal> rates;
		private LocalDateTime lastUpdated;
		private String baseCurrency = "TWD";

		public ExchangeRateDashboard(Map<String, BigDecimal> rates, LocalDateTime lastUpdated) {
			this.rates = rates;
			this.lastUpdated = lastUpdated;
		}

		// Getters
		public Map<String, BigDecimal> getRates() {
			return rates;
		}

		public LocalDateTime getLastUpdated() {
			return lastUpdated;
		}

		public String getBaseCurrency() {
			return baseCurrency;
		}
	}
}