package com.calendar.repository;

import com.calendar.model.Event;
import com.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    
    /**
     * 查詢特定用戶的所有事件
     */
    List<Event> findByUserOrderByStartTimeAsc(User user);
    
    /**
     * 查詢特定用戶在日期範圍內的事件
     */
    @Query("SELECT e FROM Event e WHERE e.user = :user AND " +
           "((e.startTime BETWEEN :startDate AND :endDate) OR " +
           "(e.endTime BETWEEN :startDate AND :endDate) OR " +
           "(e.startTime <= :startDate AND e.endTime >= :endDate))")
    List<Event> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 查詢特定用戶在特定月份的事件
     */
    @Query("SELECT e FROM Event e WHERE e.user = :user AND " +
           "YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month " +
           "ORDER BY e.startTime ASC")
    List<Event> findByUserAndYearAndMonth(
        @Param("user") User user,
        @Param("year") int year, 
        @Param("month") int month
    );
    
    /**
     * 查詢特定用戶在特定年份的事件
     */
    @Query("SELECT e FROM Event e WHERE e.user = :user AND " +
           "YEAR(e.startTime) = :year ORDER BY e.startTime ASC")
    List<Event> findByUserAndYear(
        @Param("user") User user,
        @Param("year") int year
    );
    
    // ========== 管理員統計查詢 ==========
    
    /**
     * 查詢所有用戶在特定日期範圍內的事件
     */
    @Query("SELECT e FROM Event e WHERE " +
           "((e.startTime BETWEEN :startDate AND :endDate) OR " +
           "(e.endTime BETWEEN :startDate AND :endDate) OR " +
           "(e.startTime <= :startDate AND e.endTime >= :endDate)) " +
           "ORDER BY e.startTime ASC")
    List<Event> findAllByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 統計特定日期範圍內有排程的用戶數量
     */
    @Query("SELECT COUNT(DISTINCT e.user) FROM Event e WHERE " +
           "((e.startTime BETWEEN :startDate AND :endDate) OR " +
           "(e.endTime BETWEEN :startDate AND :endDate) OR " +
           "(e.startTime <= :startDate AND e.endTime >= :endDate))")
    Long countDistinctUsersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 查詢特定日期範圍內有排程的所有用戶
     */
    @Query("SELECT DISTINCT e.user FROM Event e WHERE " +
           "((e.startTime BETWEEN :startDate AND :endDate) OR " +
           "(e.endTime BETWEEN :startDate AND :endDate) OR " +
           "(e.startTime <= :startDate AND e.endTime >= :endDate))")
    List<User> findDistinctUsersByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 查詢特定月份的所有事件
     */
    @Query("SELECT e FROM Event e WHERE " +
           "YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month " +
           "ORDER BY e.startTime ASC")
    List<Event> findAllByYearAndMonth(
        @Param("year") int year,
        @Param("month") int month
    );
    
    /**
     * 統計特定月份有排程的用戶數量
     */
    @Query("SELECT COUNT(DISTINCT e.user) FROM Event e WHERE " +
           "YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month")
    Long countDistinctUsersByYearAndMonth(
        @Param("year") int year,
        @Param("month") int month
    );
    
    /**
     * 查詢特定用戶在指定日期是否有排程
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e " +
           "WHERE e.user = :user AND " +
           "DATE(e.startTime) <= :date AND DATE(e.endTime) >= :date")
    boolean existsByUserAndDate(
        @Param("user") User user,
        @Param("date") LocalDateTime date
    );
    
 // 🔥 新增：金額統計 (Join Financial Table)
    @Query("SELECT SUM(e.financial.estimatedCost) FROM Event e WHERE ((e.startTime BETWEEN :startDate AND :endDate) OR (e.endTime BETWEEN :startDate AND :endDate) OR (e.startTime <= :startDate AND e.endTime >= :endDate))")
    BigDecimal sumEstimatedCostByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(e.financial.estimatedCost) FROM Event e WHERE YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month")
    BigDecimal sumEstimatedCostByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
