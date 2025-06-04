package com.cc.library.service.impl;

import com.cc.library.entity.BorrowRecord;
import com.cc.library.entity.User;
import com.cc.library.entity.Book;
import com.cc.library.repository.BorrowRecordRepository;
import com.cc.library.repository.UserRepository;
import com.cc.library.repository.BookRepository;
import com.cc.library.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BorrowRecord createBorrowRequest(Long userId, Long bookId, LocalDateTime dueDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies");
        }
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(dueDate);
        record.setStatus(BorrowRecord.BorrowStatus.PENDING);
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowRecord approveBorrowRequest(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        if (record.getStatus() != BorrowRecord.BorrowStatus.PENDING) {
            throw new RuntimeException("Record is not pending");
        }
        Book book = record.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        record.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        bookRepository.save(book);
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowRecord rejectBorrowRequest(Long recordId, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        if (record.getStatus() != BorrowRecord.BorrowStatus.PENDING) {
            throw new RuntimeException("Record is not pending");
        }
        record.setStatus(BorrowRecord.BorrowStatus.REJECTED);
        // 可扩展：保存拒绝原因
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));
        if (record.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
            throw new RuntimeException("Record is not borrowed");
        }
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        record.setReturnDate(LocalDateTime.now());
        bookRepository.save(book);
        return borrowRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void deleteBorrowRecord(Long id) {
        if (!borrowRecordRepository.existsById(id)) {
            throw new RuntimeException("Borrow record not found");
        }
        borrowRecordRepository.deleteById(id);
    }

    @Override
    public BorrowRecord getBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));
    }

    @Override
    public Page<BorrowRecord> getBorrowRecordsByUser(User user, Pageable pageable) {
        return borrowRecordRepository.findByUser(user, pageable);
    }

    @Override
    public List<BorrowRecord> getOverdueRecords(LocalDateTime now) {
        return borrowRecordRepository.findByStatusAndDueDateBefore(BorrowRecord.BorrowStatus.BORROWED, now);
    }

    @Override
    public List<BorrowRecord> getRecordsByStatus(BorrowRecord.BorrowStatus status) {
        return borrowRecordRepository.findByStatus(status);
    }
} 