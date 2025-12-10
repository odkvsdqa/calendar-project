package com.calendar.controller;

import com.calendar.dto.EventDTO;
import com.calendar.facade.EventFacade;
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
public class EventController {

    @Autowired
    private EventFacade eventFacade;

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(Authentication authentication) {
        // Log 交給 AOP 或 Filter 做，這裡先保持乾淨
        return ResponseEntity.ok(eventFacade.getAllEvents(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(
            @PathVariable String id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventFacade.getEventById(id, authentication));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EventDTO>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventFacade.getEventsByDateRange(start, end, authentication));
    }

    @GetMapping("/month")
    public ResponseEntity<List<EventDTO>> getEventsByMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventFacade.getEventsByMonth(year, month, authentication));
    }

    @GetMapping("/year")
    public ResponseEntity<List<EventDTO>> getEventsByYear(
            @RequestParam int year,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventFacade.getEventsByYear(year, authentication));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @RequestBody EventDTO eventDTO, // 這裡改接收 DTO
            Authentication authentication
    ) {
        EventDTO createdEvent = eventFacade.createEvent(eventDTO, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable String id,
            @RequestBody EventDTO eventDTO, // 這裡改接收 DTO
            Authentication authentication
    ) {
        // 不需要 try-catch，異常由 GlobalExceptionHandler 接住
        EventDTO updatedEvent = eventFacade.updateEvent(id, eventDTO, authentication);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String id,
            Authentication authentication
    ) {
        eventFacade.deleteEvent(id, authentication);
        return ResponseEntity.noContent().build();
    }
}