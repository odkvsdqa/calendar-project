package com.calendar.service;

import com.calendar.dto.UserPreferenceDTO;
import com.calendar.model.User;
import com.calendar.model.UserPreference;
import com.calendar.repository.UserPreferenceRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 使用者偏好設定 Service
 * v1.1 擴充：主題、時區管理
 */
@Service
public class UserPreferenceService {
    
    @Autowired
    private UserPreferenceRepository preferenceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 🔥 支援的時區清單
    private static final List<String> SUPPORTED_TIMEZONES = Arrays.asList(
        "Asia/Taipei", "Asia/Tokyo", "Asia/Seoul", "Asia/Hong_Kong",
        "Asia/Singapore", "America/New_York", "Europe/London", 
        "Europe/Paris", "Australia/Sydney", "America/Los_Angeles"
    );
    
    /**
     * 獲取使用者的語言偏好
     */
    public String getUserLanguage(String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(UserPreference::getLanguage)
                .orElse("zh-TW");
    }
    
    /**
     * 更新使用者的語言偏好 (舊版相容)
     */
    @Transactional
    public UserPreference updateUserLanguage(String userId, String language) {
        if (!isValidLanguage(language)) {
            throw new IllegalArgumentException("無效的語言代碼: " + language);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));
        
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference(user, language));
        
        preference.setLanguage(language);
        return preferenceRepository.save(preference);
    }
    
    // 🔥 v1.1 新增：獲取完整偏好設定
    public UserPreferenceDTO getUserPreferences(String userId) {
        UserPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 如果不存在，返回預設值並建立
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("使用者不存在"));
                    // 使用預設建構子初始化
                    return preferenceRepository.save(new UserPreference(user, "zh-TW"));
                });
        
        return new UserPreferenceDTO(
            pref.getLanguage(),
            pref.getTheme(),
            pref.getTimezone()
        );
    }
    
    // 🔥 v1.1 新增：更新完整偏好設定
    @Transactional
    public UserPreferenceDTO updateUserPreferences(String userId, UserPreferenceDTO dto) {
        // 驗證輸入
        if (!isValidLanguage(dto.getLanguage())) {
            throw new IllegalArgumentException("無效的語言代碼: " + dto.getLanguage());
        }
        if (!isValidTheme(dto.getTheme())) {
            throw new IllegalArgumentException("無效的主題: " + dto.getTheme());
        }
        if (!isValidTimezone(dto.getTimezone())) {
            throw new IllegalArgumentException("無效的時區: " + dto.getTimezone());
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));
        
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference(user, dto.getLanguage()));
        
        // 更新所有欄位
        preference.setLanguage(dto.getLanguage());
        preference.setTheme(dto.getTheme());
        preference.setTimezone(dto.getTimezone());
        
        UserPreference saved = preferenceRepository.save(preference);
        
        return new UserPreferenceDTO(
            saved.getLanguage(),
            saved.getTheme(),
            saved.getTimezone()
        );
    }
    
    // 🔥 v1.1 新增：獲取支援的時區清單
    public List<String> getSupportedTimezones() {
        return SUPPORTED_TIMEZONES;
    }
    
    // === 驗證方法 ===
    
    private boolean isValidLanguage(String language) {
        return language != null && 
               (language.equals("zh-TW") || 
                language.equals("en-US") || 
                language.equals("ja-JP"));
    }
    
    private boolean isValidTheme(String theme) {
        return theme != null && 
               (theme.equals("light") || 
                theme.equals("dark") || 
                theme.equals("system"));
    }
    
    private boolean isValidTimezone(String timezone) {
        return SUPPORTED_TIMEZONES.contains(timezone);
    }
}