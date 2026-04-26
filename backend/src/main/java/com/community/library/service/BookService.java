package com.community.library.service;

import com.community.library.dto.BookDTO;
import com.community.library.entity.Book;
import com.community.library.entity.Category;
import com.community.library.repository.BookRepository;
import com.community.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public Page<BookDTO> searchBooks(String keyword, Long categoryId, Book.BookStatus status, Pageable pageable) {
        Page<Book> books = bookRepository.searchBooks(keyword, categoryId, status, pageable);
        return books.map(BookDTO::fromEntity);
    }
    
    public Optional<BookDTO> findById(Long id) {
        return bookRepository.findById(id).map(BookDTO::fromEntity);
    }
    
    public Optional<Book> findBookEntityById(Long id) {
        return bookRepository.findById(id);
    }
    
    @Transactional
    public Book createBook(BookDTO bookDTO) {
        Book book = new Book();
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublisher(bookDTO.getPublisher());
        book.setPublishDate(bookDTO.getPublishDate());
        book.setDescription(bookDTO.getDescription());
        book.setCoverImage(bookDTO.getCoverImage());
        book.setTotalQuantity(bookDTO.getTotalQuantity() != null ? bookDTO.getTotalQuantity() : 1);
        book.setAvailableQuantity(bookDTO.getAvailableQuantity() != null ? bookDTO.getAvailableQuantity() : book.getTotalQuantity());
        book.setLocation(bookDTO.getLocation());
        book.setStatus(bookDTO.getStatus() != null ? bookDTO.getStatus() : Book.BookStatus.AVAILABLE);
        
        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            book.setCategory(category);
        }
        
        return bookRepository.save(book);
    }
    
    @Transactional
    public Book updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        if (bookDTO.getIsbn() != null) book.setIsbn(bookDTO.getIsbn());
        if (bookDTO.getTitle() != null) book.setTitle(bookDTO.getTitle());
        if (bookDTO.getAuthor() != null) book.setAuthor(bookDTO.getAuthor());
        if (bookDTO.getPublisher() != null) book.setPublisher(bookDTO.getPublisher());
        if (bookDTO.getPublishDate() != null) book.setPublishDate(bookDTO.getPublishDate());
        if (bookDTO.getDescription() != null) book.setDescription(bookDTO.getDescription());
        if (bookDTO.getCoverImage() != null) book.setCoverImage(bookDTO.getCoverImage());
        if (bookDTO.getTotalQuantity() != null) book.setTotalQuantity(bookDTO.getTotalQuantity());
        if (bookDTO.getAvailableQuantity() != null) book.setAvailableQuantity(bookDTO.getAvailableQuantity());
        if (bookDTO.getLocation() != null) book.setLocation(bookDTO.getLocation());
        if (bookDTO.getStatus() != null) book.setStatus(bookDTO.getStatus());
        
        if (bookDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            book.setCategory(category);
        }
        
        return bookRepository.save(book);
    }
    
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        bookRepository.delete(book);
    }
    
    @Transactional
    public void decreaseAvailableQuantity(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("图书库存不足");
        }
        
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        
        if (book.getAvailableQuantity() == 0) {
            book.setStatus(Book.BookStatus.BORROWED);
        }
        
        bookRepository.save(book);
    }
    
    @Transactional
    public void increaseAvailableQuantity(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        
        if (book.getAvailableQuantity() > 0 && book.getStatus() == Book.BookStatus.BORROWED) {
            book.setStatus(Book.BookStatus.AVAILABLE);
        }
        
        bookRepository.save(book);
    }
    
    public long getTotalBooksCount() {
        Long count = bookRepository.getTotalBooksCount();
        return count != null ? count : 0;
    }
    
    public long getAvailableBooksCount() {
        Long count = bookRepository.getAvailableBooksCount();
        return count != null ? count : 0;
    }
    
    public long getBorrowedBooksCount() {
        return bookRepository.countByStatusBorrowed();
    }
}
