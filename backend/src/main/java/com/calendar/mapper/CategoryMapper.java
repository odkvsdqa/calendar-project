package com.calendar.mapper;

import org.springframework.stereotype.Component;

import com.calendar.dto.CategoryDTO;
import com.calendar.model.EventCategory;

@Component
public class CategoryMapper {

	public CategoryDTO toDTO(EventCategory entity) {
		if (entity == null)
			return null;

		return new CategoryDTO(entity.getId(), entity.getName(), entity.getNameKey(), entity.getColor(),
				entity.getIcon(), entity.getIsSystem());
	}

	public EventCategory toEntity(CategoryDTO dto) {
		if (dto == null)
			return null;

		EventCategory entity = new EventCategory();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setNameKey(dto.getNameKey());
		entity.setColor(dto.getColor());
		entity.setIcon(dto.getIcon());
		entity.setIsSystem(dto.getIsSystem());

		return entity;
	}
}