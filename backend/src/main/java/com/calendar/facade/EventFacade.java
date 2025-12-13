package com.calendar.facade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.calendar.dto.EventDTO;
import com.calendar.mapper.EventMapper;
import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import com.calendar.model.User;
import com.calendar.repository.EventCategoryRepository;
import com.calendar.service.EventService;
import com.calendar.service.UserService;

@Component
public class EventFacade {

	@Autowired
	private EventService eventService;

	@Autowired
	private UserService userService;

	@Autowired
	private EventMapper eventMapper;

	@Autowired
	private EventCategoryRepository categoryRepository; // 🔥 新增

	private User getUser(Authentication authentication) {
		String username = authentication.getName();
		return userService.findByUsername(username).orElseThrow(() -> new RuntimeException("用戶不存在"));
	}

	public List<EventDTO> getAllEvents(Authentication authentication) {
		User user = getUser(authentication);
		List<Event> events = eventService.getAllEvents(user);
		return events.stream().map(eventMapper::toDTO).collect(Collectors.toList());
	}

	public EventDTO getEventById(String id, Authentication authentication) {
		User user = getUser(authentication);
		return eventService.getEventById(id, user).map(eventMapper::toDTO)
				.orElseThrow(() -> new RuntimeException("找不到事件或無權限"));
	}

	public List<EventDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Authentication authentication) {
		User user = getUser(authentication);
		return eventService.getEventsByDateRange(user, start, end).stream().map(eventMapper::toDTO)
				.collect(Collectors.toList());
	}

	public List<EventDTO> getEventsByMonth(int year, int month, Authentication authentication) {
		User user = getUser(authentication);
		return eventService.getEventsByMonth(user, year, month).stream().map(eventMapper::toDTO)
				.collect(Collectors.toList());
	}

	public List<EventDTO> getEventsByYear(int year, Authentication authentication) {
		User user = getUser(authentication);
		return eventService.getEventsByYear(user, year).stream().map(eventMapper::toDTO).collect(Collectors.toList());
	}

	// 🔥 修改：新增事件時處理類型
	public EventDTO createEvent(EventDTO eventDTO, Authentication authentication) {
		User user = getUser(authentication);
		Event event = eventMapper.toEntity(eventDTO);

		// 🔥 處理類型
		if (eventDTO.getCategoryId() != null) {
			EventCategory category = categoryRepository.findById(eventDTO.getCategoryId())
					.orElseThrow(() -> new RuntimeException("類型不存在"));
			event.setCategory(category);
		}

		Event savedEvent = eventService.createEvent(event, user);
		return eventMapper.toDTO(savedEvent);
	}

	// 🔥 修改：更新事件時處理類型
	public EventDTO updateEvent(String id, EventDTO eventDTO, Authentication authentication) {
		User user = getUser(authentication);
		Event eventDetails = eventMapper.toEntity(eventDTO);

		// 🔥 處理類型
		if (eventDTO.getCategoryId() != null) {
			EventCategory category = categoryRepository.findById(eventDTO.getCategoryId())
					.orElseThrow(() -> new RuntimeException("類型不存在"));
			eventDetails.setCategory(category);
		} else {
			// 如果 categoryId 為 null，表示要清除類型
			eventDetails.setCategory(null);
		}

		Event updatedEvent = eventService.updateEvent(id, eventDetails, user);
		return eventMapper.toDTO(updatedEvent);
	}

	public void deleteEvent(String id, Authentication authentication) {
		User user = getUser(authentication);
		eventService.deleteEvent(id, user);
	}
}