package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Man chi tiet khach hang (MC03), dung lai o form sua (MC04). */
@Getter
@Builder
@AllArgsConstructor
public class CustomerView {

    private Long id;
    private String code;
    private String fullName;
    private String email;
    private String mobile;
    private String address;
    private String note;
    private boolean visible;
    /** Da lien ket tai khoan thi khong doi email. */
    private boolean linkedAccount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
