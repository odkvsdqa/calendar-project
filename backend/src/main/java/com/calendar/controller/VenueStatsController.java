package com.calendar.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.model.VenueInteraction;
import com.calendar.model.VenueWish;
import com.calendar.service.VenueStatsService;

@RestController
@RequestMapping("/api/venues")
public class VenueStatsController {

	@Autowired
	private VenueStatsService statsService;

	// 1. 使用者許願 (公開或需登入皆可，這邊設為需登入)
	@PostMapping("/wish")
	public ResponseEntity<?> wishVenue(@RequestBody Map<String, String> body) {
		String name = body.get("name");
		if (name == null || name.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("場地名稱不能為空");
		}
		statsService.wishForVenue(name);
		return ResponseEntity.ok(Map.of("message", "許願成功！"));
	}

	// 2. 記錄點擊 (前端點選選單時觸發)
	@PostMapping("/{venueId}/click")
	public ResponseEntity<?> trackClick(@PathVariable String venueId) {
		statsService.trackVenueClick(venueId);
		return ResponseEntity.ok().build();
	}

	// 3. 管理員獲取許願清單
	@GetMapping("/admin/wishes")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<VenueWish>> getWishes() {
		return ResponseEntity.ok(statsService.getAllWishes());
	}

	// 4. 管理員獲取點擊統計
	@GetMapping("/admin/interactions")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<VenueInteraction>> getInteractions() {
		return ResponseEntity.ok(statsService.getAllInteractions());
	}
}