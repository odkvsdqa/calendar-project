package com.calendar.config; // 確保 package 名稱對應你的資料夾結構

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    // 將 PasswordEncoder 放在這裡，讓 UserService 可以單獨引用它
    // 從而避開與 SecurityConfig 的循環依賴
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}