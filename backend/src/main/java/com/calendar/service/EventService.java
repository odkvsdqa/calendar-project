package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    /**
     * 創建新事件（綁定到特定用戶）
     */
    @Transactional
    public Event createEvent(Event event, User user) {
        System.out.println("創建事件: " + event.getTitle() + " (用戶: " + user.getUsername() + ")");
        event.setUser(user);
        return eventRepository.save(event);
    }
    
    /**
     * 獲取特定用戶的所有事件
     */
    public List<Event> getAllEvents(User user) {
        System.out.println("查詢用戶事件: " + user.getUsername());
        return eventRepository.findByUserOrderByStartTimeAsc(user);
    }
    
    /**
     * 根據ID獲取事件（驗證所有權）
     */
    public Optional<Event> getEventById(String id, User user) {
        System.out.println("查詢事件ID: " + id);
        Optional<Event> event = eventRepository.findById(id);
        
        // 驗證事件是否屬於該用戶
        if (event.isPresent() && !event.get().getUser().getId().equals(user.getId())) {
            return Optional.empty();
        }
        
        return event;
    }
    
    /**
     * 查詢特定日期範圍內的事件
     */
    public List<Event> getEventsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        System.out.println("查詢日期範圍事件: " + startDate + " 到 " + endDate);
        return eventRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    /**
     * 查詢特定月份的事件
     */
    public List<Event> getEventsByMonth(User user, int year, int month) {
        System.out.println("查詢月份事件: " + year + "年" + month + "月");
        return eventRepository.findByUserAndYearAndMonth(user, year, month);
    }
    
    /**
     * 查詢特定年份的事件
     */
    public List<Event> getEventsByYear(User user, int year) {
        System.out.println("查詢年份事件: " + year + "年");
        return eventRepository.findByUserAndYear(user, year);
    }
    
    /**
     * 更新事件（驗證所有權）
     */
    @Transactional
    public Event updateEvent(String id, Event eventDetails, User user) {
        System.out.println("更新事件ID: " + id);
        return eventRepository.findById(id)
            .filter(event -> event.getUser().getId().equals(user.getId()))
            .map(event -> {
                event.setTitle(eventDetails.getTitle());
                event.setDescription(eventDetails.getDescription());
                event.setStartTime(eventDetails.getStartTime());
                event.setEndTime(eventDetails.getEndTime());
                event.setColor(eventDetails.getColor());
             // 🔥🔥🔥 關鍵修正：必須手動把「預計花費」從傳入的物件搬移到舊物件上
                // 因為我們有用 @Transient 和 魔法 Setter，這一行會自動更新 Financial 表
                event.setEstimatedCost(eventDetails.getEstimatedCost());
                return eventRepository.save(event);
            })
            .orElseThrow(() -> new RuntimeException("找不到事件或無權限操作"));
    }
    
    /**
     * 刪除事件（驗證所有權）
     */
    @Transactional
    public void deleteEvent(String id, User user) {
        System.out.println("刪除事件ID: " + id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到事件"));
        
        if (!event.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("無權限刪除此事件");
        }
        
        eventRepository.deleteById(id);
    }
}