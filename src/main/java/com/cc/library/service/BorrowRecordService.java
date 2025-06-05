package com.cc.library.service;

import com.cc.library.entity.BorrowRecord;
import com.cc.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowRecordService {
    BorrowRecord createBorrowRequest(Long userId, Long bookId, LocalDateTime dueDate, String remarks);
    BorrowRecord approveBorrowRequest(Long recordId);
    BorrowRecord rejectBorrowRequest(Long recordId, String reason);
    BorrowRecord returnBook(Long recordId);
    void deleteBorrowRecord(Long id);
    BorrowRecord getBorrowRecordById(Long id);
    Page<BorrowRecord> getBorrowRecordsByUser(User user, Pageable pageable);
    List<BorrowRecord> getOverdueRecords(LocalDateTime now);
    List<BorrowRecord> getRecordsByStatus(BorrowRecord.BorrowStatus status);
    Page<BorrowRecord> getAllBorrowRecords(Pageable pageable);
} 