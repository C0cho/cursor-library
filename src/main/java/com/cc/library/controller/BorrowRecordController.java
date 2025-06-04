package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.entity.BorrowRecord;
import com.cc.library.entity.User;
import com.cc.library.service.BorrowRecordService;
import com.cc.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/borrow-records")
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;
    private final UserService userService;

    @PostMapping
    public Result<BorrowRecord> createBorrowRequest(@RequestParam Long userId,
                                                    @RequestParam Long bookId,
                                                    @RequestParam LocalDateTime dueDate) {
        return Result.success(borrowRecordService.createBorrowRequest(userId, bookId, dueDate));
    }

    @PutMapping("/{id}/approve")
    public Result<BorrowRecord> approveBorrowRequest(@PathVariable Long id) {
        return Result.success(borrowRecordService.approveBorrowRequest(id));
    }

    @PutMapping("/{id}/reject")
    public Result<BorrowRecord> rejectBorrowRequest(@PathVariable Long id, @RequestParam String reason) {
        return Result.success(borrowRecordService.rejectBorrowRequest(id, reason));
    }

    @PutMapping("/{id}/return")
    public Result<BorrowRecord> returnBook(@PathVariable Long id) {
        return Result.success(borrowRecordService.returnBook(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBorrowRecord(@PathVariable Long id) {
        borrowRecordService.deleteBorrowRecord(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<BorrowRecord> getBorrowRecordById(@PathVariable Long id) {
        return Result.success(borrowRecordService.getBorrowRecordById(id));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<BorrowRecord>> getBorrowRecordsByUser(@PathVariable Long userId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(borrowRecordService.getBorrowRecordsByUser(user, pageable));
    }

    @GetMapping("/overdue")
    public Result<List<BorrowRecord>> getOverdueRecords() {
        return Result.success(borrowRecordService.getOverdueRecords(LocalDateTime.now()));
    }

    @GetMapping("/status/{status}")
    public Result<List<BorrowRecord>> getRecordsByStatus(@PathVariable BorrowRecord.BorrowStatus status) {
        return Result.success(borrowRecordService.getRecordsByStatus(status));
    }
} 