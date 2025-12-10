package com.calendar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.calendar.dto.UserDTO;
import com.calendar.dto.request.LoginRequest;
import com.calendar.dto.request.RegisterRequest;
import com.calendar.dto.response.AuthResponse;
import com.calendar.model.User;
import com.calendar.security.JwtUtil;

@Service
public class AuthService {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthenticationManager authenticationManager;

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

			// ⚠️ 加上這行 debug
			System.out.println("登入用戶: " + user.getUsername() + ", 角色: " + user.getRole());

			return new AuthResponse(token, userDTO);

		} catch (AuthenticationException e) {
			throw new RuntimeException("用戶名或密碼錯誤");
		}
	}

	/**
	 * 用戶註冊
	 */
	public UserDTO register(RegisterRequest registerRequest) {
		User user = userService.registerUser(registerRequest.getUsername(), registerRequest.getEmail(),
				registerRequest.getPassword());

		return UserDTO.fromUser(user);
	}
}