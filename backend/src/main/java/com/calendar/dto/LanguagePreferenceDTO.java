package com.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 語言偏好 DTO
 * 用於統一 API 請求/響應格式
 * 
 * 與專案其他 DTO (LoginRequest, RegisterRequest, AuthResponse) 保持一致風格
 */
public class LanguagePreferenceDTO {

    /**
     * 語言代碼
     * 可選值: zh-TW, en-US, ja-JP
     */
    @NotBlank(message = "語言代碼不可為空")
    @Pattern(
        regexp = "^(zh-TW|en-US|ja-JP)$", 
        message = "無效的語言代碼，僅支援: zh-TW, en-US, ja-JP"
    )
    private String language;

    // ==================== Constructors ====================

    public LanguagePreferenceDTO() {
    }

    public LanguagePreferenceDTO(String language) {
        this.language = language;
    }

    // ==================== Getters and Setters ====================

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    // ==================== toString (方便 Debug) ====================

    @Override
    public String toString() {
        return "LanguagePreferenceDTO{" +
                "language='" + language + '\'' +
                '}';
    }
}