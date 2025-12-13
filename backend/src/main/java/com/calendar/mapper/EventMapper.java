package com.calendar.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.calendar.dto.EventDTO;
import com.calendar.model.Event;

@Component
public class EventMapper {

	@Autowired
	private CategoryMapper categoryMapper;

	/**
	 * Entity -> DTO 前端要什麼，這裡就塞什麼
	 */
	public EventDTO toDTO(Event event) {
		if (event == null)
			return null;
		EventDTO dto = new EventDTO();
		dto.setId(event.getId());
		dto.setTitle(event.getTitle());
		dto.setDescription(event.getDescription());
		dto.setStartTime(event.getStartTime());
		dto.setEndTime(event.getEndTime());
		dto.setColor(event.getColor());
		// 假設你的 Event 邏輯裡判斷全天是用時間判斷，這裡先設預設值，或是根據邏輯轉換
		dto.setAllDay(false);

		// 🔥 處理財務欄位 (攤平)
		// 雖然你在 Entity 裡寫了 @JsonProperty，但用 DTO 模式時，我們在這裡手動轉，更安全
		dto.setEstimatedCost(event.getEstimatedCost()); // 這是你 Entity 裡的 Helper method
		dto.setCurrency(event.getCurrency()); // 這是你 Entity 裡的 Helper method

		// 🔥 新增類型轉換
		if (event.getCategory() != null) {
			dto.setCategoryId(event.getCategory().getId());
			dto.setCategory(categoryMapper.toDTO(event.getCategory()));
		}
		return dto;
	}

	/**
	 * DTO -> Entity 用於創建或更新
	 */
	public Event toEntity(EventDTO dto) {
		if (dto == null)
			return null;

		Event event = new Event();
		// ID 通常由 DB 生成，或者是 Update 時才會設，這裡先保留
		event.setId(dto.getId());
		event.setTitle(dto.getTitle());
		event.setDescription(dto.getDescription());
		event.setStartTime(dto.getStartTime());
		event.setEndTime(dto.getEndTime());
		event.setColor(dto.getColor());

		// 🔥 處理財務 (利用你在 Entity 寫好的 Helper Method)
		event.setEstimatedCost(dto.getEstimatedCost());
		event.setCurrency(dto.getCurrency());

		return event;
	}

	/**
	 * 將 DTO 的值更新到現有的 Entity (用於 Update)
	 */
	public void updateEntityFromDTO(EventDTO dto, Event entity) {
		if (dto == null || entity == null)
			return;

		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setStartTime(dto.getStartTime());
		entity.setEndTime(dto.getEndTime());
		entity.setColor(dto.getColor());

		// 更新財務
		entity.setEstimatedCost(dto.getEstimatedCost());
		entity.setCurrency(dto.getCurrency());
	}
}