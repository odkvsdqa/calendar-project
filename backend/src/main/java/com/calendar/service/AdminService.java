package com.calendar.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendar.dto.response.AdminStatsDTO;
import com.calendar.dto.response.AdminUserDTO;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventFinancialRepository;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;

@Service
public class AdminService {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private EventFinancialRepository eventFinancialRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CurrencyService currencyService;

	/**
	 * 1. 獲取所有用戶列表 回傳 List<AdminUserDTO> 取代 List<Map>
	 */
	public List<AdminUserDTO> getAllUsers() {
		return userRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	/**
	 * 2. 獲取特定日期有排程的用戶列表 回傳 AdminStatsDTO.DailyStats
	 */
	public AdminStatsDTO.DailyStats getUsersWithEventsByDate(LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

		List<User> usersWithEvents = eventRepository.findDistinctUsersByDateRange(startOfDay, endOfDay);
		List<Event> events = eventRepository.findAllByDateRange(startOfDay, endOfDay);

		// 轉換 User -> AdminUserDTO
		List<AdminUserDTO> userDTOs = usersWithEvents.stream().map(this::convertToDTO).collect(Collectors.toList());

		return new AdminStatsDTO.DailyStats(date, userDTOs.size(), events.size(), userDTOs);
	}

	/**
	 * 3. 獲取特定月份的統計數據 回傳 AdminStatsDTO.MonthlyStats
	 */
	public AdminStatsDTO.MonthlyStats getMonthlyStatistics(int year, int month) {
		List<Event> events = eventRepository.findAllByYearAndMonth(year, month);
		Long userCount = eventRepository.countDistinctUsersByYearAndMonth(year, month);

		// 金額計算
		List<Map<String, Object>> costsByCurrency = eventFinancialRepository
				.sumEstimatedCostByYearAndMonthGroupByCurrency(year, month);
		BigDecimal totalCostInTWD = calculateTotalInTWD(costsByCurrency);

		// 日統計
		Map<String, Integer> dailyUserCount = new TreeMap<>(); // TreeMap 讓日期自動排序
		events.stream().collect(Collectors.groupingBy(e -> e.getStartTime().toLocalDate()))
				.forEach((date, eventList) -> {
					long uniqueUsers = eventList.stream().map(e -> e.getUser().getId()).distinct().count();
					dailyUserCount.put(date.toString(), (int) uniqueUsers);
				});

		return new AdminStatsDTO.MonthlyStats(year, month, userCount, events.size(), totalCostInTWD, dailyUserCount);
	}

	/**
	 * 4. 獲取特定時間範圍的統計數據 回傳 AdminStatsDTO.TimeRangeStats
	 */
	public AdminStatsDTO.TimeRangeStats getTimeRangeStatistics(LocalDateTime startTime, LocalDateTime endTime) {
		List<Event> events = eventRepository.findAllByDateRange(startTime, endTime);
		Long userCount = eventRepository.countDistinctUsersByDateRange(startTime, endTime);
		List<User> users = eventRepository.findDistinctUsersByDateRange(startTime, endTime);

		// 金額計算
		List<Map<String, Object>> costsByCurrency = eventFinancialRepository
				.sumEstimatedCostByDateRangeGroupByCurrency(startTime, endTime);
		BigDecimal totalCostInTWD = calculateTotalInTWD(costsByCurrency);

		// 小時統計
		Map<String, Integer> hourlyUserCount = new TreeMap<>();
		Map<Integer, Set<String>> usersByHour = new HashMap<>();

		for (Event event : events) {
			LocalDateTime current = event.getStartTime();
			LocalDateTime eventEnd = event.getEndTime();
			while (!current.isAfter(eventEnd)) {
				if (current.isBefore(startTime) || current.isAfter(endTime)) {
					current = current.plusHours(1);
					continue; // 超出範圍的不算
				}
				usersByHour.computeIfAbsent(current.getHour(), k -> new HashSet<>()).add(event.getUser().getId());
				current = current.plusHours(1);
			}
		}

		usersByHour.forEach((hour, userSet) -> hourlyUserCount.put(String.format("%02d:00", hour), userSet.size()));

		List<AdminUserDTO> userDTOs = users.stream().map(this::convertToDTO).collect(Collectors.toList());

		return new AdminStatsDTO.TimeRangeStats(startTime, endTime, userCount, events.size(), totalCostInTWD,
				hourlyUserCount, userDTOs);
	}

	/**
	 * 5. 獲取特定用戶的所有排程日期 回傳 AdminStatsDTO.UserScheduleStats
	 */
	public AdminStatsDTO.UserScheduleStats getUserScheduleDates(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用戶不存在"));

		List<Event> events = eventRepository.findByUserOrderByStartTimeAsc(user);

		Set<String> scheduleDates = new TreeSet<>(); // TreeSet 自動排序
		for (Event event : events) {
			LocalDate start = event.getStartTime().toLocalDate();
			LocalDate end = event.getEndTime().toLocalDate();
			LocalDate current = start;
			while (!current.isAfter(end)) {
				scheduleDates.add(current.toString());
				current = current.plusDays(1);
			}
		}

		return new AdminStatsDTO.UserScheduleStats(user.getId(), user.getUsername(), scheduleDates.size(),
				new ArrayList<>(scheduleDates));
	}

	// === Private Helpers ===

	private AdminUserDTO convertToDTO(User user) {
		return new AdminUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(),
				user.getCreatedAt());
	}

	private BigDecimal calculateTotalInTWD(List<Map<String, Object>> costsByCurrency) {
		BigDecimal total = BigDecimal.ZERO;
		for (Map<String, Object> item : costsByCurrency) {
			String currency = (String) item.get("currency");
			BigDecimal amount = (BigDecimal) item.get("total");
			if (currency == null)
				currency = "TWD";
			if (amount != null) {
				total = total.add(currencyService.convertToTWD(amount, currency));
			}
		}
		return total;
	}
}