package com.calendar.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.calendar.model.EventCategory;
import com.calendar.model.User;
import com.calendar.repository.EventCategoryRepository;

@Service
public class EventCategoryService {

	@Autowired
	private EventCategoryRepository categoryRepository;

	/**
	 * 取得使用者所有可用類型（系統 + 自訂）
	 */
	public List<EventCategory> getAvailableCategories(User user) {
		return categoryRepository.findAvailableCategories(user);
	}

	/**
	 * 新增自訂類型
	 */
	@Transactional
	public EventCategory createCustomCategory(EventCategory category, User user) {
		// 檢查是否重複
		categoryRepository.findByUserAndName(user, category.getName()).ifPresent(c -> {
			throw new IllegalArgumentException("已存在同名的類型");
		});

		// 設定屬性
		category.setId(UUID.randomUUID().toString());
		category.setUser(user);
		category.setIsSystem(false);
		category.setNameKey(null); // 自訂類型不需要 i18n key

		return categoryRepository.save(category);
	}

	/**
	 * 更新自訂類型
	 */
	@Transactional
	public EventCategory updateCustomCategory(String id, EventCategory updatedCategory, User user) {
		EventCategory existing = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("類型不存在"));

		// 只能修改自己的自訂類型
		if (existing.getIsSystem() || !existing.getUser().getId().equals(user.getId())) {
			throw new IllegalArgumentException("無權限修改此類型");
		}

		// 更新欄位
		existing.setName(updatedCategory.getName());
		existing.setColor(updatedCategory.getColor());
		existing.setIcon(updatedCategory.getIcon());

		return categoryRepository.save(existing);
	}

	/**
	 * 刪除自訂類型
	 */
	@Transactional
	public void deleteCustomCategory(String id, User user) {
		EventCategory category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("類型不存在"));

		// 只能刪除自己的自訂類型
		if (category.getIsSystem() || !category.getUser().getId().equals(user.getId())) {
			throw new IllegalArgumentException("無權限刪除此類型");
		}

		// 注意：刪除類型後，相關事件的 category_id 會被設為 NULL（因為有 ON DELETE SET NULL）
		categoryRepository.delete(category);
	}
}