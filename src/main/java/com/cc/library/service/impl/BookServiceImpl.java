package com.cc.library.service.impl;

import com.cc.library.entity.Book;
import com.cc.library.entity.Category;
import com.cc.library.repository.BookRepository;
import com.cc.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Book createBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("ISBN already exists");
        }
        book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Long id, Book book) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!existingBook.getIsbn().equals(book.getIsbn()) &&
            bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("ISBN already exists");
        }

        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setCategory(book.getCategory());
        existingBook.setDescription(book.getDescription());
        existingBook.setTotalCopies(book.getTotalCopies());
        existingBook.setStatus(book.getStatus());

        return bookRepository.save(existingBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Page<Book> getBooksByCategory(Category category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable);
    }

    @Override
    public List<Book> getBooksByStatus(Book.BookStatus status) {
        return bookRepository.findByStatus(status);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    @Transactional
    public void updateBookStatus(Long id, Book.BookStatus status) {
        Book book = getBookById(id);
        book.setStatus(status);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBookStock(Long id, int quantity) {
        Book book = getBookById(id);
        int newTotalCopies = book.getTotalCopies() + quantity;
        if (newTotalCopies < 0) {
            throw new RuntimeException("Cannot reduce stock below 0");
        }
        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(book.getAvailableCopies() + quantity);
        bookRepository.save(book);
    }
} 