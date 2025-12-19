package com.calendar.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_interactions")
public class VenueInteraction {

	@Id
	@Column(length = 50)
	private String venueId; // 對應 VenueScraper 的 ID (例如: "tmc", "taipei-dome")

	@Column(nullable = false)
	private Long clickCount = 0L; // 點擊次數

	@Column(name = "last_clicked_at")
	private LocalDateTime lastClickedAt;

	public VenueInteraction() {
	}

	public VenueInteraction(String venueId) {
		this.venueId = venueId;
		this.clickCount = 1L;
		this.lastClickedAt = LocalDateTime.now();
	}

	public void incrementClick() {
		this.clickCount++;
		this.lastClickedAt = LocalDateTime.now();
	}

	// Getters & Setters
	public String getVenueId() {
		return venueId;
	}

	public Long getClickCount() {
		return clickCount;
	}

	public LocalDateTime getLastClickedAt() {
		return lastClickedAt;
	}
}