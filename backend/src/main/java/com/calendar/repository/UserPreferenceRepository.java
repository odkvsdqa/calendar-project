package com.calendar.repository;

import com.calendar.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 使用者偏好設定 Repository
 * 
 * 遵循專案 Repository 命名慣例 (EventRepository, UserRepository)
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {

    /**
     * 根據使用者 ID 查詢偏好設定
     * @param userId 使用者 ID
     * @return Optional<UserPreference>
     */
    Optional<UserPreference> findByUserId(String userId);

    /**
     * 根據使用者 ID 刪除偏好設定
     * @param userId 使用者 ID
     */
    void deleteByUserId(String userId);
}