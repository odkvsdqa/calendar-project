package com.calendar.controller;

import com.calendar.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
//@CrossOrigin(origins = {"*"}, allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")  // 確保只有管理員可以訪問
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    /**
     * 獲取所有用戶列表
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        System.out.println("管理員API: 獲取所有用戶列表");
        List<Map<String, Object>> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 獲取特定日期有排程的用戶
     * GET /api/admin/users-by-date?date=2024-11-22
     */
    @GetMapping("/users-by-date")
    public ResponseEntity<Map<String, Object>> getUsersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        System.out.println("管理員API: 查詢日期 " + date + " 的用戶排程");
        Map<String, Object> result = adminService.getUsersWithEventsByDate(date);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 獲取特定月份的統計數據
     * GET /api/admin/monthly-stats?year=2024&month=11
     */
    @GetMapping("/monthly-stats")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month
    ) {
        System.out.println("管理員API: 查詢 " + year + "年" + month + "月 統計數據");
        Map<String, Object> result = adminService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 獲取特定時間範圍的統計數據(支援小時級別)
     * GET /api/admin/time-range-stats?start=2024-11-22T09:00:00&end=2024-11-22T18:00:00
     */
    @GetMapping("/time-range-stats")
    public ResponseEntity<Map<String, Object>> getTimeRangeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        System.out.println("管理員API: 查詢時間範圍 " + start + " 到 " + end + " 的統計數據");
        Map<String, Object> result = adminService.getTimeRangeStatistics(start, end);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 獲取特定用戶的所有排程日期
     * GET /api/admin/user-schedule-dates/{userId}
     */
    @GetMapping("/user-schedule-dates/{userId}")
    public ResponseEntity<Map<String, Object>> getUserScheduleDates(
            @PathVariable String userId
    ) {
        System.out.println("管理員API: 查詢用戶 " + userId + " 的排程日期");
        try {
            Map<String, Object> result = adminService.getUserScheduleDates(userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}