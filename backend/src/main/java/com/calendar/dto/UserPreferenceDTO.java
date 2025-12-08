package com.calendar.dto;

/**
 * 使用者偏好設定 DTO
 * v1.1 統一管理：language, theme, timezone
 */
public class UserPreferenceDTO {
    
    private String language;
    private String theme;
    private String timezone;
    
    // === Constructor ===
    public UserPreferenceDTO() {}
    
    public UserPreferenceDTO(String language, String theme, String timezone) {
        this.language = language;
        this.theme = theme;
        this.timezone = timezone;
    }
    
    // === Getters & Setters ===
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}