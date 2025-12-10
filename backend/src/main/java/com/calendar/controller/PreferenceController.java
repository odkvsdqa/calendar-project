package com.calendar.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.dto.response.UserPreferenceDTO;
import com.calendar.facade.PreferenceFacade;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

	@Autowired
	private PreferenceFacade preferenceFacade;

	@GetMapping("/language")
	public ResponseEntity<Map<String, String>> getLanguage(Authentication authentication) {
		// Spring 會自動把當前登入者的資訊注入到 authentication
		return ResponseEntity.ok(preferenceFacade.getUserLanguage(authentication));
	}

	@PutMapping("/language")
	public ResponseEntity<Map<String, String>> updateLanguage(Authentication authentication,
			@RequestBody Map<String, String> request) {
		String language = request.get("language");
		return ResponseEntity.ok(preferenceFacade.updateUserLanguage(authentication, language));
	}

	@GetMapping
	public ResponseEntity<UserPreferenceDTO> getPreferences(Authentication authentication) {
		return ResponseEntity.ok(preferenceFacade.getUserPreferences(authentication));
	}

	@PutMapping
	public ResponseEntity<UserPreferenceDTO> updatePreferences(Authentication authentication,
			@RequestBody UserPreferenceDTO dto) {
		return ResponseEntity.ok(preferenceFacade.updateUserPreferences(authentication, dto));
	}
}