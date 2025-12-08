package com.calendar.controller;

import com.calendar.dto.UserPreferenceDTO;
import com.calendar.model.User;
import com.calendar.security.JwtUtil;
import com.calendar.service.UserPreferenceService;
import com.calendar.service.UserService; // 🔥 引入 UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 使用者偏好設定 Controller
 * v1.1 擴充：主題、時區管理
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {
    
    @Autowired
    private UserPreferenceService preferenceService;
    
    @Autowired
    private UserService userService; // 🔥 注入 UserService 以查詢正確 ID
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // === 原有 API（向後相容）===
    
    /**
     * 獲取使用者語言偏好
     * GET /api/preferences/language
     */
    @GetMapping("/language")
    public ResponseEntity<Map<String, String>> getLanguage(
            @RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromToken(authHeader);
        String language = preferenceService.getUserLanguage(userId);
        return ResponseEntity.ok(Map.of("language", language));
    }
    
    /**
     * 更新使用者語言偏好
     * PUT /api/preferences/language
     */
    @PutMapping("/language")
    public ResponseEntity<Map<String, String>> updateLanguage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        String userId = getUserIdFromToken(authHeader);
        String language = request.get("language");
        
        preferenceService.updateUserLanguage(userId, language);
        return ResponseEntity.ok(Map.of("language", language));
    }
    
    // 🔥 v1.1 新增 API
    
    /**
     * 獲取完整偏好設定
     * GET /api/preferences
     */
    @GetMapping
    public ResponseEntity<UserPreferenceDTO> getPreferences(
            @RequestHeader("Authorization") String authHeader) {
        // 🔥 修正：透過 UserService 轉換 username -> UUID
        String userId = getUserIdFromToken(authHeader);
        UserPreferenceDTO dto = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * 更新完整偏好設定
     * PUT /api/preferences
     */
    @PutMapping
    public ResponseEntity<UserPreferenceDTO> updatePreferences(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserPreferenceDTO dto) {
        
        String userId = getUserIdFromToken(authHeader);
        UserPreferenceDTO updated = preferenceService.updateUserPreferences(userId, dto);
        return ResponseEntity.ok(updated);
    }
    
    /**S
     * 獲取支援的時區清單
     * GET /api/preferences/timezones
     */
    @GetMapping("/timezones")
    public ResponseEntity<List<String>> getSupportedTimezones() {
        List<String> timezones = preferenceService.getSupportedTimezones();
        return ResponseEntity.ok(timezones);
    }
    
    // === 工具方法 ===
    
    /**
     * 從 JWT Token 解析出 User ID (UUID)
     * 修正：JWT 內存的是 username，需查詢 DB 轉換為 UUID
     */
    private String getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        
        // 🔥 修正：使用 .orElseThrow() 來解開 Optional
        // 如果有找到就回傳 User，沒找到就直接報錯
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found from token: " + username));
        
        return user.getId();
    }
}