package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Mot dong lich su hien tren chi tiet don. */
@Getter
@Builder
@AllArgsConstructor
public class OrderHistoryView {

    private LocalDateTime createdAt;
    private String content;
    private String actorName;
}
