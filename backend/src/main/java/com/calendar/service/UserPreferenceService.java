package com.calendar.service;

import com.calendar.model.User;
import com.calendar.model.UserPreference;
import com.calendar.repository.UserPreferenceRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用者偏好設定 Service
 * 
 * 遵循專案 Service 層設計模式 (EventService, AuthService)
 */
@Service
public class UserPreferenceService {

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 獲取使用者的語言偏好
     * 如果不存在，則返回預設值 "zh-TW"
     * 
     * @param userId 使用者 ID
     * @return 語言代碼 (zh-TW, en-US, ja-JP)
     */
    public String getUserLanguage(String userId) {
        return preferenceRepository.findByUserId(userId)
                .map(UserPreference::getLanguage)
                .orElse("zh-TW"); // 預設繁體中文
    }

    /**
     * 更新使用者的語言偏好
     * 如果偏好記錄不存在，則自動建立
     * 
     * @param userId 使用者 ID
     * @param language 語言代碼
     * @return 更新後的 UserPreference
     * @throws RuntimeException 如果使用者不存在
     */
    @Transactional
    public UserPreference updateUserLanguage(String userId, String language) {
        // 驗證語言代碼是否有效
        if (!isValidLanguage(language)) {
            throw new IllegalArgumentException("無效的語言代碼: " + language);
        }

        // 查詢使用者
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));

        // 查詢或建立偏好記錄
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference(user, language));

        // 更新語言設定
        preference.setLanguage(language);

        // 儲存並返回
        return preferenceRepository.save(preference);
    }

    /**
     * 驗證語言代碼是否有效
     * 
     * @param language 語言代碼
     * @return true 如果有效
     */
    private boolean isValidLanguage(String language) {
        return language != null && 
               (language.equals("zh-TW") || 
                language.equals("en-US") || 
                language.equals("ja-JP"));
    }
}