package com.calendar.dto;

public class CategoryDTO {
	private String id;
	private String name;
	private String nameKey; // i18n 翻譯鍵
	private String color;
	private String icon;
	private Boolean isSystem;

	public CategoryDTO() {
	}

	public CategoryDTO(String id, String name, String nameKey, String color, String icon, Boolean isSystem) {
		this.id = id;
		this.name = name;
		this.nameKey = nameKey;
		this.color = color;
		this.icon = icon;
		this.isSystem = isSystem;
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameKey() {
		return nameKey;
	}

	public void setNameKey(String nameKey) {
		this.nameKey = nameKey;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}
}