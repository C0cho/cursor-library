package com.cc.library.repository;

import com.cc.library.entity.Book;
import com.cc.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByCategory(Category category, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%")
    Page<Book> searchBooks(String keyword, Pageable pageable);
    
    boolean existsByIsbn(String isbn);
    
    List<Book> findByStatus(Book.BookStatus status);

    Page<Book> findByTitleContainingOrAuthorContaining(String title, String author, Pageable pageable);
} 