package com.cc.library.dto;

import lombok.Data;

@Data
public class BorrowRequestDto {
    private Long bookId;
    private Integer days;
    private String remarks;
} 