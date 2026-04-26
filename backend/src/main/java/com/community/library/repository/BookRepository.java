package com.community.library.repository;

import com.community.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    
    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("status") Book.BookStatus status,
                           Pageable pageable);
    
    List<Book> findByCategoryId(Long categoryId);
    
    @Query("SELECT SUM(b.totalQuantity) FROM Book b")
    Long getTotalBooksCount();
    
    @Query("SELECT SUM(b.availableQuantity) FROM Book b")
    Long getAvailableBooksCount();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = 'BORROWED'")
    long countByStatusBorrowed();
}
