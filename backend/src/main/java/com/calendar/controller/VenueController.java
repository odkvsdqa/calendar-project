package com.calendar.controller;

import com.calendar.dto.EventDTO;
import com.calendar.facade.VenueFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private VenueFacade venueFacade; // 只依賴 Facade

    @GetMapping("/list")
    public List<Map<String, String>> getAvailableVenues() {
        return venueFacade.getAvailableVenues();
    }

    @GetMapping("/{venueId}/events")
    public List<EventDTO> getEvents(@PathVariable String venueId) {
        return venueFacade.getEventsByVenue(venueId);
    }
}