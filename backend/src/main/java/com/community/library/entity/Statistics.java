package com.community.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "statistics")
public class Statistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "stat_date", unique = true, nullable = false)
    private LocalDate statDate;
    
    @Column(name = "total_users")
    private Integer totalUsers = 0;
    
    @Column(name = "total_books")
    private Integer totalBooks = 0;
    
    @Column(name = "total_borrowed")
    private Integer totalBorrowed = 0;
    
    @Column(name = "total_returned")
    private Integer totalReturned = 0;
    
    @Column(name = "total_overdue")
    private Integer totalOverdue = 0;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
