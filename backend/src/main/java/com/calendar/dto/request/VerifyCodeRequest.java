package com.calendar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyCodeRequest {

	@NotBlank(message = "Email 不能為空")
	@Email(message = "Email 格式不正確")
	private String email;

	@NotBlank(message = "用途不能為空")
	private String purpose; // register, reset, change

	// Getters and Setters
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}