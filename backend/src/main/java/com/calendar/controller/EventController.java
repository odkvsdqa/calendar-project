package com.calendar.controller;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.service.EventService;
import com.calendar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
//@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 從 Authentication 獲取當前用戶
     */
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
    }
    
    /**
     * 獲取當前用戶的所有事件
     * GET /api/events
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(Authentication authentication) {
        System.out.println("API: 獲取所有事件");
        User user = getCurrentUser(authentication);
        List<Event> events = eventService.getAllEvents(user);
        return ResponseEntity.ok(events);
    }
    
    /**
     * 根據ID獲取單個事件
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(
            @PathVariable String id,
            Authentication authentication
    ) {
        System.out.println("API: 獲取事件ID: " + id);
        User user = getCurrentUser(authentication);
        return eventService.getEventById(id, user)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根據日期範圍查詢事件
     * GET /api/events/range?start=xxx&end=xxx
     */
    @GetMapping("/range")
    public ResponseEntity<List<Event>> getEventsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        Authentication authentication
    ) {
        System.out.println("API: 查詢日期範圍事件: " + start + " 到 " + end);
        User user = getCurrentUser(authentication);
        List<Event> events = eventService.getEventsByDateRange(user, start, end);
        return ResponseEntity.ok(events);
    }
    
    /**
     * 根據年月查詢事件
     * GET /api/events/month?year=2024&month=11
     */
    @GetMapping("/month")
    public ResponseEntity<List<Event>> getEventsByMonth(
        @RequestParam int year,
        @RequestParam int month,
        Authentication authentication
    ) {
        System.out.println("API: 查詢月份事件: " + year + "年" + month + "月");
        User user = getCurrentUser(authentication);
        List<Event> events = eventService.getEventsByMonth(user, year, month);
        return ResponseEntity.ok(events);
    }
    
    /**
     * 根據年份查詢事件
     * GET /api/events/year?year=2024
     */
    @GetMapping("/year")
    public ResponseEntity<List<Event>> getEventsByYear(
            @RequestParam int year,
            Authentication authentication
    ) {
        System.out.println("API: 查詢年份事件: " + year + "年");
        User user = getCurrentUser(authentication);
        List<Event> events = eventService.getEventsByYear(user, year);
        return ResponseEntity.ok(events);
    }
    
    /**
     * 創建新事件
     * POST /api/events
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @RequestBody Event event,
            Authentication authentication
    ) {
        System.out.println("API: 創建事件: " + event.getTitle());
        User user = getCurrentUser(authentication);
        Event createdEvent = eventService.createEvent(event, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }
    
    /**
     * 更新事件
     * PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
        @PathVariable String id,
        @RequestBody Event eventDetails,
        Authentication authentication
    ) {
        System.out.println("API: 更新事件ID: " + id);
        try {
            User user = getCurrentUser(authentication);
            Event updatedEvent = eventService.updateEvent(id, eventDetails, user);
            return ResponseEntity.ok(updatedEvent);
        } catch (RuntimeException e) {
            System.err.println("更新事件失敗: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 刪除事件
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String id,
            Authentication authentication
    ) {
        System.out.println("API: 刪除事件ID: " + id);
        try {
            User user = getCurrentUser(authentication);
            eventService.deleteEvent(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("刪除事件失敗: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}