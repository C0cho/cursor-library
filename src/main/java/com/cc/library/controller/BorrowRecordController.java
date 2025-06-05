package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.entity.BorrowRecord;
import com.cc.library.entity.User;
import com.cc.library.service.BorrowRecordService;
import com.cc.library.service.UserService;
import com.cc.library.dto.BorrowRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/borrow-records")
@RequiredArgsConstructor
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecord> createBorrowRequest(@RequestBody BorrowRequestDto borrowRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            userId = ((User) authentication.getPrincipal()).getId();
        } else {
             // Handle case where user is not authenticated or principal is not User object
             return Result.error("用户未认证或认证信息无效");
        }

        if (userId == null) {
             return Result.error("无法获取当前用户信息");
        }

        LocalDateTime dueDate = LocalDateTime.now().plus(borrowRequestDto.getDays(), ChronoUnit.DAYS);

        BorrowRecord createdRecord = borrowRecordService.createBorrowRequest(
                userId,
                borrowRequestDto.getBookId(),
                dueDate,
                borrowRequestDto.getRemarks()
        );

        return Result.success(createdRecord);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BorrowRecord> approveBorrowRequest(@PathVariable Long id) {
        return Result.success(borrowRecordService.approveBorrowRequest(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BorrowRecord> rejectBorrowRequest(@PathVariable Long id, @RequestParam String reason) {
        return Result.success(borrowRecordService.rejectBorrowRequest(id, reason));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecord> returnBook(@PathVariable Long id) {
        return Result.success(borrowRecordService.returnBook(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBorrowRecord(@PathVariable Long id) {
        borrowRecordService.deleteBorrowRecord(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<BorrowRecord> getBorrowRecordById(@PathVariable Long id) {
        return Result.success(borrowRecordService.getBorrowRecordById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated() and (#userId == authentication.principal.id or hasRole('ADMIN'))")
    public Result<Page<BorrowRecord>> getBorrowRecordsByUser(@PathVariable Long userId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(borrowRecordService.getBorrowRecordsByUser(user, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<BorrowRecord>> getAllBorrowRecords(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(borrowRecordService.getAllBorrowRecords(pageable));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BorrowRecord>> getOverdueRecords() {
        return Result.success(borrowRecordService.getOverdueRecords(LocalDateTime.now()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BorrowRecord>> getRecordsByStatus(@PathVariable BorrowRecord.BorrowStatus status) {
        return Result.success(borrowRecordService.getRecordsByStatus(status));
    }
} 