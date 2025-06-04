package com.cc.library.service;

import com.cc.library.entity.Book;
import com.cc.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    Book createBook(Book book);
    Book updateBook(Long id, Book book);
    void deleteBook(Long id);
    Book getBookById(Long id);
    Page<Book> getAllBooks(Pageable pageable);
    Page<Book> getBooksByCategory(Category category, Pageable pageable);
    Page<Book> searchBooks(String keyword, Pageable pageable);
    List<Book> getBooksByStatus(Book.BookStatus status);
    boolean existsByIsbn(String isbn);
    void updateBookStatus(Long id, Book.BookStatus status);
    void updateBookStock(Long id, int quantity);
} 