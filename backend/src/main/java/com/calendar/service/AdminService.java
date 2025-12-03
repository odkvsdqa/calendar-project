package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // 新增
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 獲取所有用戶列表(不含密碼)
     */
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole().name());
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 獲取特定日期有排程的用戶列表
     */
    public Map<String, Object> getUsersWithEventsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<User> usersWithEvents = eventRepository.findDistinctUsersByDateRange(startOfDay, endOfDay);
        List<Event> events = eventRepository.findAllByDateRange(startOfDay, endOfDay);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("userCount", usersWithEvents.size());
        result.put("eventCount", events.size());
        result.put("users", usersWithEvents.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    return userMap;
                })
                .collect(Collectors.toList()));
        
        return result;
    }
    
    /**
     * 獲取特定月份的統計數據
     */
    public Map<String, Object> getMonthlyStatistics(int year, int month) {
        List<Event> events = eventRepository.findAllByYearAndMonth(year, month);
        Long userCount = eventRepository.countDistinctUsersByYearAndMonth(year, month);
     // 🔥 注入金額
        BigDecimal totalCost = eventRepository.sumEstimatedCostByYearAndMonth(year, month);
        
        // 按日期分組統計
        Map<LocalDate, List<Event>> eventsByDate = events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getStartTime().toLocalDate()
                ));
        
        // 計算每天有多少用戶有排程
        Map<String, Integer> dailyUserCount = new HashMap<>();
        for (Map.Entry<LocalDate, List<Event>> entry : eventsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            Set<String> uniqueUsers = entry.getValue().stream()
                    .map(e -> e.getUser().getId())
                    .collect(Collectors.toSet());
            dailyUserCount.put(date.toString(), uniqueUsers.size());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("totalUsers", userCount);
        result.put("totalEvents", events.size());
        result.put("totalCost", totalCost != null ? totalCost : BigDecimal.ZERO); // 🔥
        result.put("dailyUserCount", dailyUserCount);
        
        return result;
    }
    
    /**
     * 獲取特定時間範圍的統計數據(支援小時級別)
     */
    public Map<String, Object> getTimeRangeStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Event> events = eventRepository.findAllByDateRange(startTime, endTime);
        Long userCount = eventRepository.countDistinctUsersByDateRange(startTime, endTime);
        List<User> users = eventRepository.findDistinctUsersByDateRange(startTime, endTime);
        // 🔥 注入金額
        BigDecimal totalCost = eventRepository.sumEstimatedCostByDateRange(startTime, endTime);
        
        // 按小時分組統計
        Map<Integer, Set<String>> usersByHour = new HashMap<>();
        for (Event event : events) {
            LocalDateTime eventStart = event.getStartTime();
            LocalDateTime eventEnd = event.getEndTime();
            
            // 遍歷事件涵蓋的所有小時
            LocalDateTime current = eventStart;
            while (!current.isAfter(eventEnd)) {
                int hour = current.getHour();
                usersByHour.computeIfAbsent(hour, k -> new HashSet<>())
                        .add(event.getUser().getId());
                current = current.plusHours(1);
            }
        }
        
        // 轉換為每小時的用戶數量
        Map<String, Integer> hourlyUserCount = new HashMap<>();
        for (Map.Entry<Integer, Set<String>> entry : usersByHour.entrySet()) {
            hourlyUserCount.put(String.format("%02d:00", entry.getKey()), entry.getValue().size());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("totalUsers", userCount);
        result.put("totalEvents", events.size());
        result.put("totalCost", totalCost != null ? totalCost : BigDecimal.ZERO); // 🔥
        result.put("hourlyUserCount", hourlyUserCount);
        result.put("users", users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    return userMap;
                })
                .collect(Collectors.toList()));
        
        return result;
    }
    
    /**
     * 獲取特定用戶的所有排程日期
     */
    public Map<String, Object> getUserScheduleDates(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用戶不存在");
        }
        
        User user = userOpt.get();
        List<Event> events = eventRepository.findByUserOrderByStartTimeAsc(user);
        
        // 提取所有有排程的日期
        Set<LocalDate> scheduleDates = events.stream()
                .flatMap(event -> {
                    LocalDate start = event.getStartTime().toLocalDate();
                    LocalDate end = event.getEndTime().toLocalDate();
                    List<LocalDate> dates = new ArrayList<>();
                    LocalDate current = start;
                    while (!current.isAfter(end)) {
                        dates.add(current);
                        current = current.plusDays(1);
                    }
                    return dates.stream();
                })
                .collect(Collectors.toSet());
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", user.getUsername());
        result.put("totalScheduleDays", scheduleDates.size());
        result.put("scheduleDates", scheduleDates.stream()
                .sorted()
                .map(LocalDate::toString)
                .collect(Collectors.toList()));
        
        return result;
    }
}