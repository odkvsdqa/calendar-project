package com.calendar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

	@NotBlank(message = "Email 不能為空")
	@Email
	private String email;

	@NotBlank(message = "驗證碼不能為空")
	private String code;

	@NotBlank(message = "新密碼不能為空")
	@Size(min = 6, message = "密碼長度至少為 6")
	private String newPassword;

	// Getters and Setters
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}