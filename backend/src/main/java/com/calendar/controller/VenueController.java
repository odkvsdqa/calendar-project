package com.calendar.controller;

import com.calendar.dto.EventDTO;
import com.calendar.model.ExternalEvent;
import com.calendar.repository.ExternalEventRepository;
import com.calendar.service.scraper.VenueScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private List<VenueScraper> scrapers;

    @Autowired
    private ExternalEventRepository repository; // 🔥 改用 Repository

    @GetMapping("/list")
    public List<Map<String, String>> getAvailableVenues() {
        return scrapers.stream()
                .map(s -> Map.of("id", s.getVenueId(), "name", s.getVenueName()))
                .collect(Collectors.toList());
    }

    // 🔥 修改這裡：不再呼叫 scraper.scrapeEvents()，而是查 DB
    @GetMapping("/{venueId}/events")
    public List<EventDTO> getEvents(@PathVariable String venueId) {
        
        // 1. 從資料庫撈資料
        List<ExternalEvent> entities = repository.findByVenueId(venueId);

        // 2. 轉回 EventDTO 給前端
        return entities.stream().map(entity -> {
            EventDTO dto = new EventDTO();
            dto.setId(entity.getId());
            dto.setTitle(entity.getTitle());
            dto.setDescription(entity.getDescription());
            dto.setStartTime(entity.getStartTime());
            dto.setEndTime(entity.getEndTime());
            
            // 根據 venueId 給顏色 (可以在這裡寫死，或存在 DB)
            if ("tmc".equals(venueId)) {
                // 北流：原本是亮橘 #f97316 -> 改為 柔和赤陶色 (Muted Terra Cotta)
                dto.setColor("#E09F7D"); 
            } else if ("taipei-dome".equals(venueId)) {
                // 大巨蛋：原本是亮藍 #3B82F6 -> 改為 霧霾藍 (Muted Slate Blue)
                dto.setColor("#7C8DB5"); 
            } else {
                dto.setColor("#666666");
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}