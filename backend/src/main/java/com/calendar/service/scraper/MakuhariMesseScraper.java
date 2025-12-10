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
public class MakuhariMesseScraper implements VenueScraper {

    private static final Logger log = LoggerFactory.getLogger(MakuhariMesseScraper.class);
    
    // 基礎網址
    private static final String BASE_URL = "https://www.m-messe.co.jp/en/event/?hl=zh-TW";
    private static final String DOMAIN = "https://www.m-messe.co.jp";

    @Override
    public String getVenueId() {
        return "makuhari-messe";
    }

    @Override
    public String getVenueName() {
        return "幕張展覽館";
    }

    @Override
    public List<EventDTO> scrapeEvents() {
        List<EventDTO> events = new ArrayList<>();
        
        int page = 1;
        int maxPages = 10; // 安全機制：最多爬 10 頁

        while (page <= maxPages) {
            try {
                // 🔥 構造分頁網址：自動加上 &page=1, &page=2...
                String currentUrl = BASE_URL + "&page=" + page;
                log.info("🕷️ [幕張] 正在爬取第 {} 頁: {}", page, currentUrl);
                
                Document doc = Jsoup.connect(currentUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .timeout(20000)
                        .get();

                Elements cards = doc.select(".eventList li.eventInr");
                
                // 如果這一頁找不到任何活動，代表翻頁結束了
                if (cards.isEmpty()) {
                    log.info("✅ [幕張] 第 {} 頁無資料，停止爬取。", page);
                    break;
                }

                log.info("    -> 找到 {} 個活動", cards.size());

                for (Element card : cards) {
                    try {
                        String title = card.select(".eventTit").text().trim();
                        String link = card.select("a").attr("href");
                        if (!link.startsWith("http")) link = DOMAIN + link;

                        // 優先抓 data-txt，抓不到抓 text
                        String dateRaw = card.select(".date wel").attr("data-txt").trim();
                        if (dateRaw.isEmpty()) {
                            dateRaw = card.select(".date").text().trim();
                        }

                        if (!title.isEmpty() && !dateRaw.isEmpty()) {
                            EventDTO dto = parseEvent(title, dateRaw, link);
                            if (dto != null) {
                                events.add(dto);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ [幕張] 解析單筆失敗: {}", e.getMessage());
                    }
                }
                
                // 為了禮貌，每頁中間稍微暫停一下 (0.5秒)
                Thread.sleep(500);
                
                // 準備下一頁
                page++;

            } catch (IOException e) {
                log.error("❌ [幕張] 連線失敗 (第{}頁): {}", page, e.getMessage());
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("✅ [幕張] 全部爬取完成，共累積 {} 筆有效活動", events.size());
        return events;
    }

    private EventDTO parseEvent(String title, String dateRaw, String link) {
        // Log 方便除錯
        // log.info("🔍 [Debug] 解析日期: [{}] - {}", dateRaw, title);

        // Regex: 4位數字 + 非數字 + 1~2位數字 + 非數字 + 1~2位數字
        Pattern p = Pattern.compile("(\\d{4})\\D+(\\d{1,2})\\D+(\\d{1,2})");
        Matcher m = p.matcher(dateRaw);

        List<LocalDate> dates = new ArrayList<>();
        
        while (m.find()) {
            try {
                String year = m.group(1);
                String month = String.format("%02d", Integer.parseInt(m.group(2)));
                String day = String.format("%02d", Integer.parseInt(m.group(3)));
                
                String isoDate = year + "-" + month + "-" + day;
                dates.add(LocalDate.parse(isoDate));
            } catch (Exception e) {
                log.warn("    -> 日期轉換異常: {}", m.group());
            }
        }

        if (!dates.isEmpty()) {
            EventDTO dto = new EventDTO();
            dto.setId(UUID.randomUUID().toString());
            dto.setTitle("[幕張] " + title);
            dto.setDescription("原始連結: " + link + "\n完整日期: " + dateRaw);
            
            LocalDate start = dates.get(0);
            LocalDate end = dates.size() > 1 ? dates.get(dates.size() - 1) : start;

            dto.setStartTime(LocalDateTime.of(start, LocalTime.of(9, 0)));
            dto.setEndTime(LocalDateTime.of(end, LocalTime.of(17, 0)));
            dto.setColor("#009688"); 
            return dto;
        }
        return null;
    }
}