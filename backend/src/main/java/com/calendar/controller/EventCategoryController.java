package com.calendar.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.dto.CategoryDTO;
import com.calendar.facade.EventCategoryFacade;

@RestController
@RequestMapping("/api/categories")
public class EventCategoryController {

	@Autowired
	private EventCategoryFacade categoryFacade;

	/**
	 * 取得所有可用類型（系統 + 自訂） GET /api/categories
	 */
	@GetMapping
	public ResponseEntity<List<CategoryDTO>> getAvailableCategories(Authentication authentication) {
		return ResponseEntity.ok(categoryFacade.getAvailableCategories(authentication));
	}

	/**
	 * 新增自訂類型 POST /api/categories
	 */
	@PostMapping
	public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO,
			Authentication authentication) {
		CategoryDTO created = categoryFacade.createCustomCategory(categoryDTO, authentication);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	/**
	 * 更新自訂類型 PUT /api/categories/{id}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<CategoryDTO> updateCategory(@PathVariable String id, @RequestBody CategoryDTO categoryDTO,
			Authentication authentication) {
		CategoryDTO updated = categoryFacade.updateCustomCategory(id, categoryDTO, authentication);
		return ResponseEntity.ok(updated);
	}

	/**
	 * 刪除自訂類型 DELETE /api/categories/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable String id, Authentication authentication) {
		categoryFacade.deleteCustomCategory(id, authentication);
		return ResponseEntity.noContent().build();
	}
}