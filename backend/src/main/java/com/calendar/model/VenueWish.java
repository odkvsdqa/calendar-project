package com.calendar.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_wishes")
public class VenueWish {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String venueName; // 場地名稱 (例如: "高雄巨蛋")

	@Column(nullable = false)
	private Integer count = 0; // 被許願次數

	@Column(name = "last_requested_at")
	private LocalDateTime lastRequestedAt;

	public VenueWish() {
	}

	public VenueWish(String venueName) {
		this.venueName = venueName;
		this.count = 1;
		this.lastRequestedAt = LocalDateTime.now();
	}

	public void incrementCount() {
		this.count++;
		this.lastRequestedAt = LocalDateTime.now();
	}

	// Getters & Setters
	public Long getId() {
		return id;
	}

	public String getVenueName() {
		return venueName;
	}

	public Integer getCount() {
		return count;
	}

	public LocalDateTime getLastRequestedAt() {
		return lastRequestedAt;
	}
}