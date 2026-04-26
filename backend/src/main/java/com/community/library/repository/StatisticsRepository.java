package com.community.library.repository;

import com.community.library.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    Optional<Statistics> findByStatDate(LocalDate statDate);
    
    List<Statistics> findByStatDateBetweenOrderByStatDate(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT s FROM Statistics s WHERE s.statDate >= :startDate AND s.statDate <= :endDate ORDER BY s.statDate")
    List<Statistics> findStatisticsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
