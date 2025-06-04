package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.entity.Book;
import com.cc.library.entity.Category;
import com.cc.library.service.BookService;
import com.cc.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;

    @PostMapping
    public Result<Book> createBook(@RequestBody Book book) {
        return Result.success(bookService.createBook(book));
    }

    @PutMapping("/{id}")
    public Result<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return Result.success(bookService.updateBook(id, book));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Book> getBookById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping
    public Result<Page<Book>> getAllBooks(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(bookService.getAllBooks(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public Result<Page<Book>> getBooksByCategory(@PathVariable Long categoryId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Category category = categoryService.getCategoryById(categoryId);
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(bookService.getBooksByCategory(category, pageable));
    }

    @GetMapping("/search")
    public Result<Page<Book>> searchBooks(@RequestParam String keyword,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(bookService.searchBooks(keyword, pageable));
    }

    @GetMapping("/status/{status}")
    public Result<List<Book>> getBooksByStatus(@PathVariable Book.BookStatus status) {
        return Result.success(bookService.getBooksByStatus(status));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateBookStatus(@PathVariable Long id, @RequestParam Book.BookStatus status) {
        bookService.updateBookStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> updateBookStock(@PathVariable Long id, @RequestParam int quantity) {
        bookService.updateBookStock(id, quantity);
        return Result.success();
    }
} 