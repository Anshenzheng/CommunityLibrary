package com.community.library.repository;

import com.community.library.entity.BorrowRecord;
import com.community.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {
    
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    
    Page<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status, Pageable pageable);
    
    Page<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status, Pageable pageable);
    
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.dueDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId AND br.status IN ('PENDING', 'APPROVED', 'BORROWED')")
    long countActiveBorrowsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status")
    long countByStatus(@Param("status") BorrowRecord.BorrowStatus status);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    long countByBorrowDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.returnDate BETWEEN :startDate AND :endDate")
    long countByReturnDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByBookIdAndStatusIn(Long bookId, List<BorrowRecord.BorrowStatus> statuses);
}
