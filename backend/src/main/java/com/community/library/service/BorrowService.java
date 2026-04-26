package com.community.library.service;

import com.community.library.dto.BorrowRecordDTO;
import com.community.library.entity.Book;
import com.community.library.entity.BorrowRecord;
import com.community.library.entity.User;
import com.community.library.repository.BookRepository;
import com.community.library.repository.BorrowRecordRepository;
import com.community.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {
    
    private static final int DEFAULT_BORROW_DAYS = 30;
    private static final BigDecimal DAILY_FINE = new BigDecimal("0.5");
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookService bookService;
    
    @Transactional
    public BorrowRecord createBorrowRequest(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("该图书已无库存");
        }
        
        long activeBorrows = borrowRecordRepository.countActiveBorrowsByUserId(userId);
        if (activeBorrows >= 5) {
            throw new RuntimeException("您已借阅5本图书，请先归还后再借阅");
        }
        
        boolean alreadyBorrowed = borrowRecordRepository.existsByBookIdAndStatusIn(
                bookId,
                Arrays.asList(BorrowRecord.BorrowStatus.PENDING, BorrowRecord.BorrowStatus.APPROVED, BorrowRecord.BorrowStatus.BORROWED)
        );
        if (alreadyBorrowed) {
            throw new RuntimeException("您已借阅或申请借阅该图书");
        }
        
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(DEFAULT_BORROW_DAYS));
        record.setStatus(BorrowRecord.BorrowStatus.PENDING);
        
        return borrowRecordRepository.save(record);
    }
    
    @Transactional
    public BorrowRecord approveBorrow(Long recordId, Long adminId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (record.getStatus() != BorrowRecord.BorrowStatus.PENDING) {
            throw new RuntimeException("该借阅申请已处理");
        }
        
        Book book = record.getBook();
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("该图书已无库存");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        bookService.decreaseAvailableQuantity(book.getId());
        
        record.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        record.setAdmin(admin);
        
        return borrowRecordRepository.save(record);
    }
    
    @Transactional
    public BorrowRecord rejectBorrow(Long recordId, Long adminId, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (record.getStatus() != BorrowRecord.BorrowStatus.PENDING) {
            throw new RuntimeException("该借阅申请已处理");
        }
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        
        record.setStatus(BorrowRecord.BorrowStatus.REJECTED);
        record.setAdmin(admin);
        record.setRejectReason(reason);
        
        return borrowRecordRepository.save(record);
    }
    
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (record.getStatus() != BorrowRecord.BorrowStatus.BORROWED && 
            record.getStatus() != BorrowRecord.BorrowStatus.OVERDUE) {
            throw new RuntimeException("该图书未在借阅状态");
        }
        
        bookService.increaseAvailableQuantity(record.getBook().getId());
        
        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        
        if (record.getStatus() == BorrowRecord.BorrowStatus.OVERDUE || 
            LocalDate.now().isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            if (overdueDays > 0) {
                record.setFineAmount(DAILY_FINE.multiply(BigDecimal.valueOf(overdueDays)));
            }
        }
        
        return borrowRecordRepository.save(record);
    }
    
    @Transactional
    public void markOverdueRecords() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(LocalDate.now());
        
        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);
        }
    }
    
    public Page<BorrowRecordDTO> findByUserId(Long userId, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByUserId(userId, pageable);
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    public Page<BorrowRecordDTO> findByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByUserIdAndStatus(userId, status, pageable);
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    public Page<BorrowRecordDTO> findAllRecords(String keyword, BorrowRecord.BorrowStatus status, Pageable pageable) {
        Specification<BorrowRecord> spec = Specification.where(null);
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("user").get("username"), "%" + keyword + "%"),
                            cb.like(root.get("user").get("realName"), "%" + keyword + "%"),
                            cb.like(root.get("book").get("title"), "%" + keyword + "%"),
                            cb.like(root.get("book").get("isbn"), "%" + keyword + "%")
                    )
            );
        }
        
        Page<BorrowRecord> records = borrowRecordRepository.findAll(spec, pageable);
        return records.map(BorrowRecordDTO::fromEntity);
    }
    
    public Optional<BorrowRecord> findById(Long id) {
        return borrowRecordRepository.findById(id);
    }
    
    public long getPendingCount() {
        return borrowRecordRepository.countByStatus(BorrowRecord.BorrowStatus.PENDING);
    }
    
    public long getBorrowedCount() {
        return borrowRecordRepository.countByStatus(BorrowRecord.BorrowStatus.BORROWED);
    }
    
    public long getOverdueCount() {
        return borrowRecordRepository.countByStatus(BorrowRecord.BorrowStatus.OVERDUE);
    }
    
    public long getBorrowsBetween(LocalDate startDate, LocalDate endDate) {
        return borrowRecordRepository.countByBorrowDateBetween(startDate, endDate);
    }
    
    public long getReturnsBetween(LocalDate startDate, LocalDate endDate) {
        return borrowRecordRepository.countByReturnDateBetween(startDate, endDate);
    }
}
