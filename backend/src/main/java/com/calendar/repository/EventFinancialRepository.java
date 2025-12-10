package com.calendar.repository;

import com.calendar.model.EventFinancial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventFinancialRepository extends JpaRepository<EventFinancial, String> {
    
    /**
     * 查詢特定月份各幣別總金額
     */
    @Query("SELECT ef.currency as currency, SUM(ef.estimatedCost) as total " +
           "FROM EventFinancial ef " +
           "JOIN ef.event e " +
           "WHERE YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month " +
           "GROUP BY ef.currency")
    List<Map<String, Object>> sumEstimatedCostByYearAndMonthGroupByCurrency(
        @Param("year") int year, 
        @Param("month") int month
    );
    
    /**
     * 查詢特定時間範圍各幣別總金額
     */
    @Query("SELECT ef.currency as currency, SUM(ef.estimatedCost) as total " +
           "FROM EventFinancial ef " +
           "JOIN ef.event e " +
           "WHERE e.startTime >= :start AND e.startTime <= :end " +
           "GROUP BY ef.currency")
    List<Map<String, Object>> sumEstimatedCostByDateRangeGroupByCurrency(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
}