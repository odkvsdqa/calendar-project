package com.calendar.dto.response;

/**
 * 使用者偏好設定 DTO v1.1 統一管理：language, theme
 */
public class UserPreferenceDTO {

	private String language;
	private String theme;

	// === Constructor ===
	public UserPreferenceDTO() {
	}

	public UserPreferenceDTO(String language, String theme) {
		this.language = language;
		this.theme = theme;
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

}