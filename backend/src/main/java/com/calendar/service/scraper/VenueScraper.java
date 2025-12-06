package com.calendar.service.scraper;

import com.calendar.dto.EventDTO;
import java.util.List;

public interface VenueScraper {
    /**
     * 回傳該場館的唯一識別碼 (例如: "taipei-dome")
     */
    String getVenueId();

    /**
     * 回傳顯示名稱 (例如: "臺北大巨蛋")
     */
    String getVenueName();

    /**
     * 執行爬蟲並回傳事件列表
     */
    List<EventDTO> scrapeEvents();
}