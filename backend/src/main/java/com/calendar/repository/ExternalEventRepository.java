package com.calendar.repository;

import com.calendar.model.ExternalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // 1. 引入這個
import org.springframework.data.jpa.repository.Query;     // 2. 引入這個
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalEventRepository extends JpaRepository<ExternalEvent, String> {
    
    // 根據場館 ID 查詢
    List<ExternalEvent> findByVenueId(String venueId);

 // 🔥 修改這裡：改用 @Query 直接下 SQL 指令，避開 JPA 狀態檢查
    @Modifying
    @Query("DELETE FROM ExternalEvent e WHERE e.venueId = :venueId")
    void deleteByVenueId(String venueId);
}