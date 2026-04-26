package com.community.library.dto;

import com.community.library.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String userRealName;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowRecord.BorrowStatus status;
    private Long adminId;
    private String adminName;
    private String rejectReason;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    
    public static BorrowRecordDTO fromEntity(BorrowRecord record) {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(record.getId());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setStatus(record.getStatus());
        dto.setRejectReason(record.getRejectReason());
        dto.setFineAmount(record.getFineAmount());
        dto.setFinePaid(record.getFinePaid());
        
        if (record.getUser() != null) {
            dto.setUserId(record.getUser().getId());
            dto.setUsername(record.getUser().getUsername());
            dto.setUserRealName(record.getUser().getRealName());
        }
        
        if (record.getBook() != null) {
            dto.setBookId(record.getBook().getId());
            dto.setBookTitle(record.getBook().getTitle());
            dto.setBookAuthor(record.getBook().getAuthor());
            dto.setBookIsbn(record.getBook().getIsbn());
        }
        
        if (record.getAdmin() != null) {
            dto.setAdminId(record.getAdmin().getId());
            dto.setAdminName(record.getAdmin().getRealName());
        }
        
        return dto;
    }
}
