package com.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.calendar.model.VenueInteraction;

public interface VenueInteractionRepository extends JpaRepository<VenueInteraction, String> {
}