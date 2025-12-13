package com.calendar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.calendar.model.EventCategory;
import com.calendar.model.User;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, String> {

	/**
	 * 查詢系統預設類型
	 */
	List<EventCategory> findByIsSystemTrue();

	/**
	 * 查詢使用者自訂類型
	 */
	List<EventCategory> findByUser(User user);

	/**
	 * 查詢使用者的所有可用類型（系統 + 自訂）
	 */
	@Query("SELECT c FROM EventCategory c WHERE c.isSystem = true OR c.user = :user")
	List<EventCategory> findAvailableCategories(User user);

	/**
	 * 檢查使用者是否已有同名類型
	 */
	Optional<EventCategory> findByUserAndName(User user, String name);
}