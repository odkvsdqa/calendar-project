package com.calendar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.calendar.model.VenueWish;

public interface VenueWishRepository extends JpaRepository<VenueWish, Long> {
	Optional<VenueWish> findByVenueName(String venueName);

	// 用於管理員排序顯示
	List<VenueWish> findAllByOrderByCountDesc();
}