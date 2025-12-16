package com.calendar.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.dto.UserDTO;
import com.calendar.dto.request.LoginRequest;
import com.calendar.dto.request.RegisterRequest;
import com.calendar.dto.request.ResetPasswordRequest;
import com.calendar.dto.response.AuthResponse;
import com.calendar.service.AuthService;
import com.calendar.service.VerificationService; // 🔥 記得引入這個

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private VerificationService verificationService; // 🔥 注入驗證碼服務

	/**
	 * 用戶登入
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			System.out.println("登入請求: " + loginRequest.getUsername());
			AuthResponse response = authService.login(loginRequest);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.err.println("登入失敗: " + e.getMessage());
			Map<String, String> error = new HashMap<>();
			error.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}
	}

	/**
	 * 🔥 新增：發送驗證碼 (整合 IP 限流與 Email 檢查)
	 */
	@PostMapping("/send-code")
	public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request, HttpServletRequest servletRequest) {
		try {
			String email = request.get("email");
			String purpose = request.get("purpose"); // "register", "reset", "change"

			// 1. 邏輯檢查
			boolean exists = authService.emailExists(email);

			if ("register".equals(purpose)) {
				if (exists) {
					return ResponseEntity.badRequest().body(Map.of("message", "此 Email 已被註冊，請直接登入"));
				}
			} else if ("reset".equals(purpose) || "change".equals(purpose)) {
				if (!exists) {
					return ResponseEntity.badRequest().body(Map.of("message", "此 Email 尚未註冊"));
				}
			}

			// 2. 發送驗證碼 (含 IP 限流)
			verificationService.sendCode(email, purpose, servletRequest);

			return ResponseEntity.ok(Map.of("message", "驗證碼已發送至您的信箱"));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 🔥 修改：用戶註冊 (加入驗證碼檢查)
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
		try {
			System.out.println("註冊請求: " + registerRequest.getUsername());

			// 1. 驗證 Redis 中的驗證碼
			if (!verificationService.verify(registerRequest.getEmail(), "register", registerRequest.getCode())) {
				return ResponseEntity.badRequest().body(Map.of("message", "驗證碼錯誤或已過期"));
			}

			// 2. 執行註冊
			UserDTO user = authService.register(registerRequest);

			Map<String, Object> response = new HashMap<>();
			response.put("message", "註冊成功");
			response.put("user", user);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			System.err.println("註冊失敗: " + e.getMessage());
			Map<String, String> error = new HashMap<>();
			error.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

	/**
	 * 🔥 新增：重設密碼 (忘記密碼)
	 */
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		try {
			// 驗證代碼
			if (!verificationService.verify(request.getEmail(), "reset", request.getCode())) {
				return ResponseEntity.badRequest().body(Map.of("message", "驗證碼錯誤或已過期"));
			}

			authService.resetPassword(request.getEmail(), request.getNewPassword());
			return ResponseEntity.ok(Map.of("message", "密碼重設成功，請重新登入"));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 🔥 新增：修改密碼 (需登入)
	 */
	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Authentication auth) {
		try {
			String username = auth.getName();
			String email = authService.getEmailByUsername(username); // 取得當前用戶 Email

			String code = request.get("code");
			String newPassword = request.get("newPassword");

			// 驗證碼檢查
			if (!verificationService.verify(email, "change", code)) {
				return ResponseEntity.badRequest().body(Map.of("message", "驗證碼錯誤或已過期"));
			}

			// 修改密碼
			authService.changePassword(username, newPassword);
			return ResponseEntity.ok(Map.of("message", "密碼修改成功"));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 登出
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logout() {
		System.out.println("用戶登出");
		Map<String, String> response = new HashMap<>();
		response.put("message", "登出成功");
		return ResponseEntity.ok(response);
	}
}