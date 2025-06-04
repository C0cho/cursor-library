package com.cc.library.service;

import com.cc.library.entity.Book;
import com.cc.library.entity.Category;
import com.cc.library.repository.BookRepository;
import com.cc.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBook() {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book createdBook = bookService.createBook(book);
        assertNotNull(createdBook);
        assertEquals("Test Book", createdBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Updated Book");
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book updatedBook = bookService.updateBook(1L, book);
        assertNotNull(updatedBook);
        assertEquals("Updated Book", updatedBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void deleteBook() {
        when(bookRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(anyLong());

        bookService.deleteBook(1L);
        verify(bookRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void getBookById() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        Book foundBook = bookService.getBookById(1L);
        assertNotNull(foundBook);
        assertEquals(1L, foundBook.getId());
    }

    @Test
    void getAllBooks() {
        Book book1 = new Book();
        Book book2 = new Book();
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book1, book2));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        Page<Book> books = bookService.getAllBooks(PageRequest.of(0, 10));
        assertNotNull(books);
        assertEquals(2, books.getTotalElements());
    }

    @Test
    void getBooksByCategory() {
        Category category = new Category();
        category.setId(1L);
        Book book1 = new Book();
        Book book2 = new Book();
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book1, book2));
        when(bookRepository.findByCategory(any(Category.class), any(Pageable.class))).thenReturn(bookPage);

        Page<Book> books = bookService.getBooksByCategory(category, PageRequest.of(0, 10));
        assertNotNull(books);
        assertEquals(2, books.getTotalElements());
    }

    @Test
    void searchBooks() {
        Book book1 = new Book();
        Book book2 = new Book();
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book1, book2));
        when(bookRepository.findByTitleContainingOrAuthorContaining(anyString(), anyString(), any(Pageable.class))).thenReturn(bookPage);

        Page<Book> books = bookService.searchBooks("test", PageRequest.of(0, 10));
        assertNotNull(books);
        assertEquals(2, books.getTotalElements());
    }
} 