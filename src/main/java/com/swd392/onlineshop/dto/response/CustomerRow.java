package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mot dong cua danh sach khach hang (MC01). */
@Getter
@Builder
@AllArgsConstructor
public class CustomerRow {

    private Long id;
    private String code;
    private String fullName;
    private String mobile;
    private String email;
    private LocalDateTime createdAt;
    private boolean visible;
}
