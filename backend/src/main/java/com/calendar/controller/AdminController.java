package com.calendar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.dto.response.AdminStatsDTO; // 注意這裡引用的路徑
import com.calendar.dto.response.AdminUserDTO; // 注意這裡引用的路徑
import com.calendar.service.AdminService;
import com.calendar.service.CurrencyService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private static final Logger log = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private AdminService adminService;

	@Autowired
	private CurrencyService currencyService;

	/**
	 * 1. 獲取所有用戶列表 改為回傳 List<AdminUserDTO>
	 */
	@GetMapping("/users")
	public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
		log.info("管理員API: 獲取所有用戶列表");
		return ResponseEntity.ok(adminService.getAllUsers());
	}

	/**
	 * 2. 獲取特定日期有排程的用戶 改為回傳 AdminStatsDTO.DailyStats
	 */
	@GetMapping("/users-by-date")
	public ResponseEntity<AdminStatsDTO.DailyStats> getUsersByDate(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		log.info("管理員API: 查詢日期 {} 的用戶排程", date);
		return ResponseEntity.ok(adminService.getUsersWithEventsByDate(date));
	}

	/**
	 * 3. 獲取特定月份的統計數據 改為回傳 AdminStatsDTO.MonthlyStats
	 */
	@GetMapping("/monthly-stats")
	public ResponseEntity<AdminStatsDTO.MonthlyStats> getMonthlyStats(@RequestParam int year, @RequestParam int month) {
		log.info("管理員API: 查詢 {} 年 {} 月 統計數據", year, month);
		return ResponseEntity.ok(adminService.getMonthlyStatistics(year, month));
	}

	/**
	 * 4. 獲取特定時間範圍的統計數據 改為回傳 AdminStatsDTO.TimeRangeStats
	 */
	@GetMapping("/time-range-stats")
	public ResponseEntity<AdminStatsDTO.TimeRangeStats> getTimeRangeStats(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		log.info("管理員API: 查詢時間範圍 {} 到 {} 的統計數據", start, end);
		return ResponseEntity.ok(adminService.getTimeRangeStatistics(start, end));
	}

	/**
	 * 5. 獲取特定用戶的所有排程日期 改為回傳 AdminStatsDTO.UserScheduleStats 移除 try-catch，交給
	 * GlobalExceptionHandler
	 */
	@GetMapping("/user-schedule-dates/{userId}")
	public ResponseEntity<AdminStatsDTO.UserScheduleStats> getUserScheduleDates(@PathVariable String userId) {
		log.info("管理員API: 查詢用戶 {} 的排程日期", userId);
		return ResponseEntity.ok(adminService.getUserScheduleDates(userId));
	}

	// ========================================
	// 匯率相關 API
	// ========================================

	@GetMapping("/exchange-rates")
	public ResponseEntity<AdminStatsDTO.ExchangeRateDashboard> getExchangeRates() {
		log.info("管理員API: 獲取匯率資訊");
		return ResponseEntity.ok(new AdminStatsDTO.ExchangeRateDashboard(currencyService.getCurrentRates(),
				currencyService.getLastUpdateTime()));
	}

	@PostMapping("/update-exchange-rates")
	public ResponseEntity<Map<String, String>> updateExchangeRates() {
		log.info("管理員API: 手動更新匯率");
		currencyService.updateRatesFromApi();
		return ResponseEntity.ok(Map.of("message", "匯率更新成功", "timestamp", LocalDateTime.now().toString()));
	}
}