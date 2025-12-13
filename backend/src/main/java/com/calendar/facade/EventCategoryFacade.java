package com.calendar.facade;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.calendar.dto.CategoryDTO;
import com.calendar.mapper.CategoryMapper;
import com.calendar.model.EventCategory;
import com.calendar.model.User;
import com.calendar.service.EventCategoryService;
import com.calendar.service.UserService;

@Component
public class EventCategoryFacade {

	@Autowired
	private EventCategoryService categoryService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryMapper categoryMapper;

	private User getUser(Authentication authentication) {
		String username = authentication.getName();
		return userService.findByUsername(username).orElseThrow(() -> new RuntimeException("用戶不存在"));
	}

	/**
	 * 取得所有可用類型（系統 + 自訂）
	 */
	public List<CategoryDTO> getAvailableCategories(Authentication authentication) {
		User user = getUser(authentication);
		return categoryService.getAvailableCategories(user).stream().map(categoryMapper::toDTO)
				.collect(Collectors.toList());
	}

	/**
	 * 新增自訂類型
	 */
	public CategoryDTO createCustomCategory(CategoryDTO dto, Authentication authentication) {
		User user = getUser(authentication);
		EventCategory entity = categoryMapper.toEntity(dto);
		EventCategory saved = categoryService.createCustomCategory(entity, user);
		return categoryMapper.toDTO(saved);
	}

	/**
	 * 更新自訂類型
	 */
	public CategoryDTO updateCustomCategory(String id, CategoryDTO dto, Authentication authentication) {
		User user = getUser(authentication);
		EventCategory entity = categoryMapper.toEntity(dto);
		EventCategory updated = categoryService.updateCustomCategory(id, entity, user);
		return categoryMapper.toDTO(updated);
	}

	/**
	 * 刪除自訂類型
	 */
	public void deleteCustomCategory(String id, Authentication authentication) {
		User user = getUser(authentication);
		categoryService.deleteCustomCategory(id, user);
	}
}