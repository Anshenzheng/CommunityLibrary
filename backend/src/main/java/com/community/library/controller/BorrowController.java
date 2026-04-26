package com.community.library.controller;

import com.community.library.dto.ApiResponse;
import com.community.library.dto.BorrowRecordDTO;
import com.community.library.dto.PageResponse;
import com.community.library.entity.BorrowRecord;
import com.community.library.entity.User;
import com.community.library.service.BorrowService;
import com.community.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin(origins = "http://localhost:4200")
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/request")
    public ResponseEntity<?> createBorrowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long bookId) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            BorrowRecord record = borrowService.createBorrowRequest(user.get().getId(), bookId);
            return ResponseEntity.ok(ApiResponse.success("借阅申请已提交", BorrowRecordDTO.fromEntity(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<?> getMyBorrowRecords(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) BorrowRecord.BorrowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BorrowRecordDTO> records;
        
        if (status != null) {
            records = borrowService.findByUserIdAndStatus(user.get().getId(), status, pageable);
        } else {
            records = borrowService.findByUserId(user.get().getId(), pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(records)));
    }
    
    @PostMapping("/return/{recordId}")
    public ResponseEntity<?> returnBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long recordId) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        try {
            BorrowRecord record = borrowService.returnBook(recordId);
            return ResponseEntity.ok(ApiResponse.success("还书成功", BorrowRecordDTO.fromEntity(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBorrowRecord(@PathVariable Long id) {
        Optional<BorrowRecord> record = borrowService.findById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(BorrowRecordDTO.fromEntity(record.get())));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
