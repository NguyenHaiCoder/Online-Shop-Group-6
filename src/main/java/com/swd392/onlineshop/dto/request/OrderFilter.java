package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Bo loc danh sach don. page tren URL bat dau tu 1. */
@Getter
@Setter
@NoArgsConstructor
public class OrderFilter {

    private LocalDate from;
    private LocalDate to;
    private String keyword;
    private Long saleId;
    private OrderStatus status;
    private String sort;
    private String dir;
    private Integer page;
}
