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
public class TaipeiDomeScraper implements VenueScraper {

    private static final Logger log = LoggerFactory.getLogger(TaipeiDomeScraper.class);
    private static final String TARGET_URL = "https://www.farglorydome.com.tw/news/";
    private static final String DOMAIN = "https://www.farglorydome.com.tw";

    @Override
    public String getVenueId() {
        return "taipei-dome";
    }

    @Override
    public String getVenueName() {
        return "臺北大巨蛋";
    }

    @Override
    public List<EventDTO> scrapeEvents() {
        List<EventDTO> events = new ArrayList<>();
        Set<String> processedTitles = new HashSet<>();

        try {
            log.info("🕷️ [大巨蛋] 開始連線: {}", TARGET_URL);
            
            // 1. 偽裝成瀏覽器連線 (增加 Header)
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .timeout(20000) 
                    .get();

            // 🔥 Debug: 印出網頁標題，確認有連到正確頁面
            log.info("🕷️ [大巨蛋] 網頁標題: {}", doc.title());

            // 2. 嘗試多種 Selector
            Elements cards = doc.select(".newsList .item"); 
            if (cards.isEmpty()) {
                log.warn("⚠️ 找不到 .newsList .item，嘗試搜尋 .item...");
                cards = doc.select(".item");
            }

            log.info("🕷️ [大巨蛋] 找到 {} 個 .item 元素", cards.size());

            // 如果完全沒抓到，印出部分 HTML 來檢查 (前 500 字)
            if (cards.isEmpty()) {
                log.error("❌ 抓取失敗，HTML 預覽:\n{}", doc.html().substring(0, Math.min(doc.html().length(), 500)));
                return events;
            }

            for (Element card : cards) {
                try {
                    // 標題 Debug
                    String title = card.select("a").attr("title").trim();
                    if (title.isEmpty()) title = card.select("h3").text().trim();
                    
                    // 日期 Debug
                    String dateRaw = card.select(".newsDate").text();
                    
                    // Log 每一個找到的東西，看看是不是格式問題
                    // log.info("🔍 掃描中: 標題=[{}] 日期=[{}]", title, dateRaw);

                    if (processedTitles.contains(title)) continue;

                    String link = card.select("a").attr("href");
                    if (!link.startsWith("http")) link = DOMAIN + link;

                    if (!title.isEmpty() && !dateRaw.isEmpty()) {
                        EventDTO dto = parseEvent(title, dateRaw, link);
                        if (dto != null) {
                            events.add(dto);
                            processedTitles.add(title);
                        } else {
                            log.warn("⚠️ 日期解析失敗: {}", dateRaw);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 解析單筆失敗: {}", e.getMessage());
                }
            }
            log.info("✅ [大巨蛋] 最終有效活動數: {}", events.size());

        } catch (IOException e) {
            log.error("❌ [大巨蛋] 連線失敗: {}", e.getMessage());
        }

        return events;
    }

    private EventDTO parseEvent(String title, String dateRaw, String link) {
        // 嘗試解析格式：2025/09/19 或 2025.09.19
        Pattern p = Pattern.compile("(\\d{4})[./-](\\d{2})[./-](\\d{2})");
        Matcher m = p.matcher(dateRaw);

        if (m.find()) {
            try {
                int year = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int day = Integer.parseInt(m.group(3));
                
                LocalDate startDate = LocalDate.of(year, month, day);
                
                EventDTO dto = new EventDTO();
                dto.setId(UUID.randomUUID().toString());
                dto.setTitle("[大巨蛋] " + title);
                dto.setDescription("原始連結: " + link + "\n完整日期: " + dateRaw);
                dto.setStartTime(LocalDateTime.of(startDate, LocalTime.of(18, 0)));
                dto.setEndTime(LocalDateTime.of(startDate, LocalTime.of(21, 0)));
                dto.setColor("#475569"); 
                return dto;
            } catch (Exception e) {
                log.error("日期轉換錯誤: {}", dateRaw);
            }
        }
        return null;
    }
}