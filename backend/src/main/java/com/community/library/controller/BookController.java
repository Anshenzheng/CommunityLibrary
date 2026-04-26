package com.community.library.controller;

import com.community.library.dto.ApiResponse;
import com.community.library.dto.BookDTO;
import com.community.library.dto.PageResponse;
import com.community.library.entity.Book;
import com.community.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:4200")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Book.BookStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime,desc") String[] sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        Page<BookDTO> books = bookService.searchBooks(keyword, categoryId, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.fromPage(books)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<BookDTO> book = bookService.findById(id);
        
        if (book.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(book.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBook(@RequestBody BookDTO bookDTO) {
        try {
            Book book = bookService.createBook(bookDTO);
            return ResponseEntity.ok(ApiResponse.success("图书创建成功", BookDTO.fromEntity(book)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        try {
            Book book = bookService.updateBook(id, bookDTO);
            return ResponseEntity.ok(ApiResponse.success("图书更新成功", BookDTO.fromEntity(book)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(ApiResponse.success("图书删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    private Pageable createPageable(int page, int size, String[] sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String property = "id";
        
        if (sort != null && sort.length > 0) {
            String sortParam = sort[0];
            String[] parts = sortParam.split(",");
            property = parts[0];
            if (parts.length > 1) {
                direction = Sort.Direction.fromString(parts[1].toUpperCase());
            }
        }
        
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
