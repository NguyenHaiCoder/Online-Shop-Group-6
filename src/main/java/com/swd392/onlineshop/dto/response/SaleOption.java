package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Mot Sale dang ACTIVE, dung cho bo loc va form giao don. */
@Getter
@Builder
@AllArgsConstructor
public class SaleOption {

    private Long id;
    private String fullName;
}
