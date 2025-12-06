package com.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 🔥 加入這個


@SpringBootApplication
@EnableScheduling // 🔥 啟用排程註解
public class CalendarApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CalendarApplication.class, args);
        System.out.println("=================================");
        System.out.println("日曆應用程式已啟動！");
        System.out.println("API 端點: http://localhost:8080/calendar-web/api/events");
        System.out.println("=================================");
    }
}