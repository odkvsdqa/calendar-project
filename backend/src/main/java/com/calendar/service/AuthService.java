package com.calendar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.calendar.dto.UserDTO;
import com.calendar.dto.request.LoginRequest;
import com.calendar.dto.request.RegisterRequest;
import com.calendar.dto.response.AuthResponse;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.calendar.security.JwtUtil;

@Service
public class AuthService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository; // 🔥 新增：直接操作 DB 用於檢查

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder passwordEncoder; // 🔥 新增：用於重設密碼

	public String getEmailByUsername(String username) {
		return userRepository.findByUsername(username).map(User::getEmail)
				.orElseThrow(() -> new RuntimeException("用戶不存在"));
	}

	/**
	 * 用戶登入
	 */
	public AuthResponse login(LoginRequest loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			String token = jwtUtil.generateToken(loginRequest.getUsername());

			User user = userService.findByUsername(loginRequest.getUsername())
					.orElseThrow(() -> new RuntimeException("用戶不存在"));

			UserDTO userDTO = UserDTO.fromUser(user);
			System.out.println("登入用戶: " + user.getUsername() + ", 角色: " + user.getRole());

			return new AuthResponse(token, userDTO);

		} catch (AuthenticationException e) {
			throw new RuntimeException("用戶名或密碼錯誤");
		}
	}

	/**
	 * 用戶註冊 (修改：移除重複檢查，交由 Controller 或 UserService 處理)
	 */
	public UserDTO register(RegisterRequest registerRequest) {
		// 這裡不需要再檢查驗證碼，因為 Controller 層已經驗證過了
		User user = userService.registerUser(registerRequest.getUsername(), registerRequest.getEmail(),
				registerRequest.getPassword());

		return UserDTO.fromUser(user);
	}

	/**
	 * 🔥 新增：檢查 Email 是否存在
	 */
	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	/**
	 * 🔥 新增：重設密碼 (忘記密碼用)
	 */
	@Transactional
	public void resetPassword(String email, String newPassword) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("用戶不存在"));

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	/**
	 * 🔥 新增：修改密碼 (登入後用，根據 username)
	 */
	@Transactional
	public void changePassword(String username, String newPassword) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("用戶不存在"));

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}
}