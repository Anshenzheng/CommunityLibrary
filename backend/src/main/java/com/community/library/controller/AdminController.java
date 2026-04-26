package com.community.library.controller;

import com.community.library.dto.*;
import com.community.library.entity.BorrowRecord;
import com.community.library.entity.Statistics;
import com.community.library.entity.User;
import com.community.library.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private ExcelExportService excelExportService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        Map<String, Object> stats = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserDTO> users = userService.findAllUsers(keyword, role, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(users)));
    }
    
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        try {
            String statusStr = request.get("status");
            User.UserStatus status = User.UserStatus.valueOf(statusStr);
            
            User user = userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", UserDTO.fromEntity(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        try {
            String roleStr = request.get("role");
            User.Role role = User.Role.valueOf(roleStr);
            
            User user = userService.updateUserRole(userId, role);
            return ResponseEntity.ok(ApiResponse.success("用户角色更新成功", UserDTO.fromEntity(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/borrow-records")
    public ResponseEntity<?> getBorrowRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BorrowRecord.BorrowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BorrowRecordDTO> records = borrowService.findAllRecords(keyword, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(records)));
    }
    
    @PostMapping("/borrow-records/{recordId}/approve")
    public ResponseEntity<?> approveBorrow(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> admin = userService.findByUsername(userDetails.getUsername());
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            BorrowRecord record = borrowService.approveBorrow(recordId, admin.get().getId());
            return ResponseEntity.ok(ApiResponse.success("借阅申请已批准", BorrowRecordDTO.fromEntity(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/borrow-records/{recordId}/reject")
    public ResponseEntity<?> rejectBorrow(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> admin = userService.findByUsername(userDetails.getUsername());
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String reason = request.get("reason");
        
        try {
            BorrowRecord record = borrowService.rejectBorrow(recordId, admin.get().getId(), reason);
            return ResponseEntity.ok(ApiResponse.success("借阅申请已拒绝", BorrowRecordDTO.fromEntity(record)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        List<Statistics> stats = statisticsService.getStatisticsByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) User.UserStatus status) throws IOException {
        
        Page<UserDTO> users = userService.findAllUsers(keyword, role, status, PageRequest.of(0, 10000));
        byte[] data = excelExportService.exportUsers(users.getContent());
        
        String filename = "用户列表_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    @GetMapping("/export/books")
    public ResponseEntity<byte[]> exportBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) throws IOException {
        
        Page<BookDTO> books = bookService.searchBooks(keyword, categoryId, null, PageRequest.of(0, 10000));
        byte[] data = excelExportService.exportBooks(books.getContent());
        
        String filename = "图书列表_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    @GetMapping("/export/borrow-records")
    public ResponseEntity<byte[]> exportBorrowRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BorrowRecord.BorrowStatus status) throws IOException {
        
        Page<BorrowRecordDTO> records = borrowService.findAllRecords(keyword, status, PageRequest.of(0, 10000));
        byte[] data = excelExportService.exportBorrowRecords(records.getContent());
        
        String filename = "借阅记录_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    @GetMapping("/export/statistics")
    public ResponseEntity<byte[]> exportStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws IOException {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        List<Statistics> stats = statisticsService.getStatisticsByDateRange(start, end);
        byte[] data = excelExportService.exportStatistics(stats);
        
        String filename = "统计数据_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
