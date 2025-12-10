package com.calendar.facade;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.calendar.dto.response.UserPreferenceDTO;
import com.calendar.model.User;
import com.calendar.service.UserPreferenceService;
import com.calendar.service.UserService;

@Component
public class PreferenceFacade {

	@Autowired
	private UserPreferenceService preferenceService;

	@Autowired
	private UserService userService;

	// Helper: 直接從 Security Context 拿當前登入者
	private User getCurrentUser(Authentication authentication) {
		String username = authentication.getName();
		return userService.findByUsername(username).orElseThrow(() -> new RuntimeException("用戶不存在"));
	}

	public Map<String, String> getUserLanguage(Authentication authentication) {
		User user = getCurrentUser(authentication);
		String language = preferenceService.getUserLanguage(user.getId());
		return Map.of("language", language);
	}

	public Map<String, String> updateUserLanguage(Authentication authentication, String language) {
		User user = getCurrentUser(authentication);
		preferenceService.updateUserLanguage(user.getId(), language);
		return Map.of("language", language);
	}

	public UserPreferenceDTO getUserPreferences(Authentication authentication) {
		User user = getCurrentUser(authentication);
		return preferenceService.getUserPreferences(user.getId());
	}

	public UserPreferenceDTO updateUserPreferences(Authentication authentication, UserPreferenceDTO dto) {
		User user = getCurrentUser(authentication);
		return preferenceService.updateUserPreferences(user.getId(), dto);
	}
}