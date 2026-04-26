package com.community.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String isbn;
    
    @Column(nullable = false)
    private String title;
    
    private String author;
    
    private String publisher;
    
    @Column(name = "publish_date")
    private LocalDate publishDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 1;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 1;
    
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;
    
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
    
    public enum BookStatus {
        AVAILABLE, BORROWED, MAINTENANCE
    }
}
