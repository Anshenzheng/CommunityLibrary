package com.community.library.service;

import com.community.library.dto.BookDTO;
import com.community.library.dto.BorrowRecordDTO;
import com.community.library.dto.UserDTO;
import com.community.library.entity.Statistics;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public byte[] exportUsers(List<UserDTO> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("用户列表");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            String[] headers = {"ID", "用户名", "真实姓名", "邮箱", "电话", "角色", "状态", "创建时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (UserDTO user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId() != null ? user.getId() : 0);
                row.createCell(1).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(2).setCellValue(user.getRealName() != null ? user.getRealName() : "");
                row.createCell(3).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(5).setCellValue(user.getRole() != null ? user.getRole().name() : "");
                row.createCell(6).setCellValue(user.getStatus() != null ? user.getStatus().name() : "");
                
                Cell dateCell = row.createCell(7);
                if (user.getCreateTime() != null) {
                    dateCell.setCellValue(user.getCreateTime().format(DATETIME_FORMATTER));
                    dateCell.setCellStyle(dateStyle);
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    public byte[] exportBooks(List<BookDTO> books) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("图书列表");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            String[] headers = {"ID", "ISBN", "书名", "作者", "出版社", "出版日期", "分类", "库存总数", "可用数量", "位置", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (BookDTO book : books) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(book.getId() != null ? book.getId() : 0);
                row.createCell(1).setCellValue(book.getIsbn() != null ? book.getIsbn() : "");
                row.createCell(2).setCellValue(book.getTitle() != null ? book.getTitle() : "");
                row.createCell(3).setCellValue(book.getAuthor() != null ? book.getAuthor() : "");
                row.createCell(4).setCellValue(book.getPublisher() != null ? book.getPublisher() : "");
                
                Cell pubDateCell = row.createCell(5);
                if (book.getPublishDate() != null) {
                    pubDateCell.setCellValue(book.getPublishDate().format(DATE_FORMATTER));
                    pubDateCell.setCellStyle(dateStyle);
                }
                
                row.createCell(6).setCellValue(book.getCategoryName() != null ? book.getCategoryName() : "");
                row.createCell(7).setCellValue(book.getTotalQuantity() != null ? book.getTotalQuantity() : 0);
                row.createCell(8).setCellValue(book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0);
                row.createCell(9).setCellValue(book.getLocation() != null ? book.getLocation() : "");
                row.createCell(10).setCellValue(book.getStatus() != null ? book.getStatus().name() : "");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    public byte[] exportBorrowRecords(List<BorrowRecordDTO> records) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("借阅记录");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            String[] headers = {"ID", "用户名", "真实姓名", "书名", "作者", "ISBN", "借阅日期", "应还日期", "归还日期", "状态", "管理员", "逾期金额", "是否已缴"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (BorrowRecordDTO record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId() != null ? record.getId() : 0);
                row.createCell(1).setCellValue(record.getUsername() != null ? record.getUsername() : "");
                row.createCell(2).setCellValue(record.getUserRealName() != null ? record.getUserRealName() : "");
                row.createCell(3).setCellValue(record.getBookTitle() != null ? record.getBookTitle() : "");
                row.createCell(4).setCellValue(record.getBookAuthor() != null ? record.getBookAuthor() : "");
                row.createCell(5).setCellValue(record.getBookIsbn() != null ? record.getBookIsbn() : "");
                
                Cell borrowDateCell = row.createCell(6);
                if (record.getBorrowDate() != null) {
                    borrowDateCell.setCellValue(record.getBorrowDate().format(DATE_FORMATTER));
                    borrowDateCell.setCellStyle(dateStyle);
                }
                
                Cell dueDateCell = row.createCell(7);
                if (record.getDueDate() != null) {
                    dueDateCell.setCellValue(record.getDueDate().format(DATE_FORMATTER));
                    dueDateCell.setCellStyle(dateStyle);
                }
                
                Cell returnDateCell = row.createCell(8);
                if (record.getReturnDate() != null) {
                    returnDateCell.setCellValue(record.getReturnDate().format(DATE_FORMATTER));
                    returnDateCell.setCellStyle(dateStyle);
                }
                
                row.createCell(9).setCellValue(record.getStatus() != null ? record.getStatus().name() : "");
                row.createCell(10).setCellValue(record.getAdminName() != null ? record.getAdminName() : "");
                row.createCell(11).setCellValue(record.getFineAmount() != null ? record.getFineAmount().doubleValue() : 0.0);
                row.createCell(12).setCellValue(record.getFinePaid() != null && record.getFinePaid() ? "是" : "否");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    public byte[] exportStatistics(List<Statistics> statistics) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("统计数据");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            String[] headers = {"统计日期", "总用户数", "总图书数", "借阅数", "归还数", "逾期数"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (Statistics stat : statistics) {
                Row row = sheet.createRow(rowNum++);
                
                Cell dateCell = row.createCell(0);
                if (stat.getStatDate() != null) {
                    dateCell.setCellValue(stat.getStatDate().format(DATE_FORMATTER));
                    dateCell.setCellStyle(dateStyle);
                }
                
                row.createCell(1).setCellValue(stat.getTotalUsers() != null ? stat.getTotalUsers() : 0);
                row.createCell(2).setCellValue(stat.getTotalBooks() != null ? stat.getTotalBooks() : 0);
                row.createCell(3).setCellValue(stat.getTotalBorrowed() != null ? stat.getTotalBorrowed() : 0);
                row.createCell(4).setCellValue(stat.getTotalReturned() != null ? stat.getTotalReturned() : 0);
                row.createCell(5).setCellValue(stat.getTotalOverdue() != null ? stat.getTotalOverdue() : 0);
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }
}
