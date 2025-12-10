package com.calendar.facade;

import com.calendar.dto.EventDTO;
import com.calendar.mapper.VenueMapper;
import com.calendar.model.ExternalEvent;
import com.calendar.repository.ExternalEventRepository;
import com.calendar.service.scraper.VenueScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VenueFacade {

    @Autowired
    private List<VenueScraper> scrapers;

    @Autowired
    private ExternalEventRepository repository;
    
    @Autowired
    private VenueMapper venueMapper;

    public List<Map<String, String>> getAvailableVenues() {
        return scrapers.stream()
                .map(s -> Map.of("id", s.getVenueId(), "name", s.getVenueName()))
                .collect(Collectors.toList());
    }

    public List<EventDTO> getEventsByVenue(String venueId) {
        // 1. 從 DB 拿資料
        List<ExternalEvent> entities = repository.findByVenueId(venueId);
        
        // 2. 透過 Mapper 轉成 DTO (顏色邏輯在 Mapper 裡)
        return entities.stream()
                .map(venueMapper::toDTO)
                .collect(Collectors.toList());
    }
}