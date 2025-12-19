package com.calendar.mapper;

import org.springframework.stereotype.Component;

import com.calendar.dto.EventDTO;
import com.calendar.model.ExternalEvent;

@Component
public class VenueMapper {

	public EventDTO toDTO(ExternalEvent entity) {
		if (entity == null)
			return null;

		EventDTO dto = new EventDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setStartTime(entity.getStartTime());
		dto.setEndTime(entity.getEndTime());

		// 🔥 將原本 Controller 裡的顏色邏輯移到這裡
		dto.setColor(determineColor(entity.getVenueId()));

		return dto;
	}

	private String determineColor(String venueId) {
		if (venueId == null)
			return "#666666";

		// 🔥 新增假日的顏色邏輯
		if (venueId.startsWith("holiday-")) {
			return "#dc2626"; // 統一用紅色 (Red-600)
		}

		switch (venueId) {
		case "tmc":
			return "#E09F7D"; // 柔和赤陶色
		case "taipei-dome":
			return "#475569"; // 灰藍
		case "makuhari-messe":
			return "#009688"; // 藍綠色
		default:
			return "#666666";
		}
	}
}