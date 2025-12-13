package com.calendar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventDTO {
	private String id;
	private String title;
	private String description;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String color;
	private boolean allDay = false;

	// 🔥 新增財務欄位
	private BigDecimal estimatedCost;
	private String currency = "TWD"; // 預設新台幣

	public EventDTO() {
	}

	public EventDTO(String id, String title, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.title = title;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	// === Getters and Setters ===

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	// 🔥 新增 Getters & Setters
	public BigDecimal getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	// 在現有的 EventDTO.java 中新增：

	private String categoryId; // 🔥 新增
	private CategoryDTO category; // 🔥 新增（完整資訊）

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public CategoryDTO getCategory() {
		return category;
	}

	public void setCategory(CategoryDTO category) {
		this.category = category;
	}
}