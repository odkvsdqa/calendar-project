package com.calendar.dto.response;

import java.time.LocalDateTime;

public class AdminUserDTO {
	private String id;
	private String username;
	private String email;
	private String role;
	private LocalDateTime createdAt;

	public AdminUserDTO(String id, String username, String email, String role, LocalDateTime createdAt) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.role = role;
		this.createdAt = createdAt;
	}

	// Getters & Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}