package com.cc.library.repository;

import com.cc.library.entity.BorrowRecord;
import com.cc.library.entity.User;
import com.cc.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    Page<BorrowRecord> findByUser(User user, Pageable pageable);
    
    List<BorrowRecord> findByStatusAndDueDateBefore(BorrowRecord.BorrowStatus status, LocalDateTime date);
    
    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);
    
    boolean existsByUserAndBookAndStatusIn(User user, Book book, List<BorrowRecord.BorrowStatus> statuses);
} 