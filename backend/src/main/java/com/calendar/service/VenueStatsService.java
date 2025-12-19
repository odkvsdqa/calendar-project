package com.calendar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.calendar.model.VenueInteraction;
import com.calendar.model.VenueWish;
import com.calendar.repository.VenueInteractionRepository;
import com.calendar.repository.VenueWishRepository;

@Service
public class VenueStatsService {

	@Autowired
	private VenueWishRepository wishRepository;

	@Autowired
	private VenueInteractionRepository interactionRepository;

	/**
	 * 使用者許願
	 */
	@Transactional
	public void wishForVenue(String venueName) {
		String cleanName = venueName.trim();
		VenueWish wish = wishRepository.findByVenueName(cleanName).orElse(new VenueWish(cleanName));

		// 如果已經存在，這行會加 1；如果是新的，建構子已經設為 1
		if (wish.getId() != null) {
			wish.incrementCount();
		}
		wishRepository.save(wish);
	}

	/**
	 * 記錄場地被點擊
	 */
	@Transactional
	public void trackVenueClick(String venueId) {
		VenueInteraction interaction = interactionRepository.findById(venueId).orElse(new VenueInteraction(venueId));

		if (interaction.getLastClickedAt() != null) { // 判斷是否為已存在的物件
			interaction.incrementClick();
		}
		interactionRepository.save(interaction);
	}

	/**
	 * Admin: 獲取許願列表
	 */
	public List<VenueWish> getAllWishes() {
		return wishRepository.findAllByOrderByCountDesc();
	}

	/**
	 * Admin: 獲取點擊統計
	 */
	public List<VenueInteraction> getAllInteractions() {
		return interactionRepository.findAll();
	}
}