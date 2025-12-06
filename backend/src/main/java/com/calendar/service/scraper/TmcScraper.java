package com.calendar.service.scraper;

import com.calendar.dto.EventDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TmcScraper implements VenueScraper {

    private static final Logger log = LoggerFactory.getLogger(TmcScraper.class);
    
    // 北流活動列表網址
    private static final String TARGET_URL = "https://tmc.taipei/show/";
    
    // 日期格式解析器 (針對網頁上的 2025.12.06 格式)
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Override
    public String getVenueId() {
        return "tmc";
    }

    @Override
    public String getVenueName() {
        return "臺北流行音樂中心";
    }

    @Override
    public List<EventDTO> scrapeEvents() {
        List<EventDTO> events = new ArrayList<>();
        
        // 🔥 關鍵去重容器：用來存已經抓過的「標題」
        // 如果網頁上有兩個活動標題一模一樣，第二次出現時會被忽略
        Set<String> processedTitles = new HashSet<>(); 

        try {
            log.info("🕷️ 正在爬取北流網頁: {}", TARGET_URL);
            
            // 1. 下載 HTML
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // 偽裝成瀏覽器
                    .timeout(10000)
                    .get();

            // 2. 選取所有活動卡片 (根據之前的 CSS Selector 偵查結果)
            Elements cards = doc.select(".c-card-clip-wrap");

            for (Element card : cards) {
                try {
                    // 3. 抓取資料
                    String title = card.select(".c-card-clip__title").text().trim();
                    String link = card.attr("href");
                    String dateRaw = card.select(".date").text();

                    // 4. 🔥 去重檢查：如果這個標題已經處理過了，直接跳過 (解決兩行問題)
                    if (processedTitles.contains(title)) {
                        log.debug("跳過重複標題: {}", title);
                        continue;
                    }

                    if (!title.isEmpty() && !dateRaw.isEmpty()) {
                        // 解析日期並轉換為 DTO
                        EventDTO dto = parseEvent(title, dateRaw, link);
                        
                        if (dto != null) {
                            events.add(dto);
                            processedTitles.add(title); // ✅ 標記此標題已處理
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 略過單筆解析失敗的活動: {}", e.getMessage());
                }
            }
            log.info("✅ 北流爬取完成，共找到 {} 筆有效活動", events.size());

        } catch (IOException e) {
            log.error("❌ 北流網站連線失敗", e);
        }

        return events;
    }

    /**
     * 解析邏輯：從字串中提取日期並設定時間
     */
    private EventDTO parseEvent(String title, String dateRaw, String link) {
        // 使用正則表達式抓取第一個日期 (格式如 2025.01.01)
        Pattern p = Pattern.compile("(\\d{4}\\.\\d{2}\\.\\d{2})");
        Matcher m = p.matcher(dateRaw);

        if (m.find()) {
            String startDateStr = m.group(1); 
            
            try {
                // 字串轉日期物件
                LocalDate startDate = LocalDate.parse(startDateStr, DATE_FMT);
                
                // 建立 DTO
                EventDTO dto = new EventDTO();
                dto.setId(UUID.randomUUID().toString());
                // 前端顯示時加上 [北流] 前綴，讓使用者一眼識別
                dto.setTitle("[北流] " + title);
                
                // 描述欄位放入連結與完整日期字串
                dto.setDescription("原始連結: " + link + "\n完整日期: " + dateRaw);
                
                // 設定時間：預設 10:00 ~ 12:00 (因為網站沒給具體時間)
                dto.setStartTime(LocalDateTime.of(startDate, LocalTime.of(10, 0)));
                dto.setEndTime(LocalDateTime.of(startDate, LocalTime.of(12, 0)));
                
                // 設定顏色 (備用，主要由 Controller 決定)
                dto.setColor("#f97316"); 

                return dto;
            } catch (Exception e) {
                log.warn("日期格式解析錯誤: {} -> {}", dateRaw, e.getMessage());
            }
        }
        return null; // 若沒抓到日期則回傳 null
    }
}