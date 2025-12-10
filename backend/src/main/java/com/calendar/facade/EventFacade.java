package com.calendar.facade;

import com.calendar.dto.EventDTO;
import com.calendar.mapper.EventMapper;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.service.EventService;
import com.calendar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventFacade {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventMapper eventMapper;

    /**
     * 內部 Helper: 從 Auth 取得 User，找不到就拋錯
     */
    private User getUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
    }

    public List<EventDTO> getAllEvents(Authentication authentication) {
        User user = getUser(authentication);
        List<Event> events = eventService.getAllEvents(user);
        // 轉換 List<Entity> -> List<DTO>
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getEventById(String id, Authentication authentication) {
        User user = getUser(authentication);
        return eventService.getEventById(id, user)
                .map(eventMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("找不到事件或無權限"));
    }

    public List<EventDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Authentication authentication) {
        User user = getUser(authentication);
        return eventService.getEventsByDateRange(user, start, end).stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<EventDTO> getEventsByMonth(int year, int month, Authentication authentication) {
        User user = getUser(authentication);
        return eventService.getEventsByMonth(user, year, month).stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> getEventsByYear(int year, Authentication authentication) {
        User user = getUser(authentication);
        return eventService.getEventsByYear(user, year).stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EventDTO createEvent(EventDTO eventDTO, Authentication authentication) {
        User user = getUser(authentication);
        
        // 1. DTO -> Entity
        Event event = eventMapper.toEntity(eventDTO);
        
        // 2. 呼叫 Service
        Event savedEvent = eventService.createEvent(event, user);
        
        // 3. Entity -> DTO
        return eventMapper.toDTO(savedEvent);
    }

    public EventDTO updateEvent(String id, EventDTO eventDTO, Authentication authentication) {
        User user = getUser(authentication);
        
        // 這裡稍微取巧，我們利用 Mapper 轉一個暫時的 Entity 傳給 Service
        // 因為你的 Service.updateEvent 是接收 Entity
        Event eventDetails = eventMapper.toEntity(eventDTO);
        
        Event updatedEvent = eventService.updateEvent(id, eventDetails, user);
        return eventMapper.toDTO(updatedEvent);
    }

    public void deleteEvent(String id, Authentication authentication) {
        User user = getUser(authentication);
        eventService.deleteEvent(id, user);
    }
}