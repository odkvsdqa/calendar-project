package com.calendar.controller;

import com.calendar.dto.LanguagePreferenceDTO;
import com.calendar.model.User;
import com.calendar.model.UserPreference;
import com.calendar.service.UserPreferenceService;
import com.calendar.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用者偏好設定 Controller
 * 處理語言偏好的讀取與更新
 * 
 * 遵循專案 Controller 設計風格 (AuthController, EventController)
 * 使用 DTO 統一請求/響應格式
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    @Autowired
    private UserPreferenceService preferenceService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 獲取當前使用者的語言偏好
     * 
     * @param userDetails Spring Security 自動注入的使用者資訊
     * @return LanguagePreferenceDTO
     */
    @GetMapping("/language")
    public ResponseEntity<?> getLanguage(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            // 🔥 修正：先取得 username，再查詢 User Entity
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("找不到使用者: " + username));
            
            String language = preferenceService.getUserLanguage(user.getId());

            // 使用 DTO 統一響應格式
            LanguagePreferenceDTO response = new LanguagePreferenceDTO(language);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "無法取得語言偏好: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 更新當前使用者的語言偏好
     * 
     * @param userDetails Spring Security 自動注入的使用者資訊
     * @param request LanguagePreferenceDTO (自動驗證 @Valid)
     * @param bindingResult Spring Validation 結果
     * @return LanguagePreferenceDTO 或錯誤訊息
     */
    @PutMapping("/language")
    public ResponseEntity<?> updateLanguage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LanguagePreferenceDTO request,
            BindingResult bindingResult
    ) {
        // 檢查 Bean Validation 錯誤
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("驗證失敗");
            
            Map<String, String> error = new HashMap<>();
            error.put("error", errorMessage);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // 🔥 修正：先取得 username，再查詢 User Entity
            String username = userDetails.getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("找不到使用者: " + username));

            // 更新語言偏好
            UserPreference preference = preferenceService.updateUserLanguage(
                user.getId(), 
                request.getLanguage()
            );

            // 使用 DTO 統一響應格式
            LanguagePreferenceDTO response = new LanguagePreferenceDTO(
                preference.getLanguage()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 無效的語言代碼 (雖然 DTO 已驗證，但保留雙重防護)
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            // 其他錯誤
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}