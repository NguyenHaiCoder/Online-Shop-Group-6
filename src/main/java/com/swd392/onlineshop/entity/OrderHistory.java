package com.swd392.onlineshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mot dong tren timeline "Lich su don hang" cua Sale / Sale Manager.
 * Vi du: "Don duoc tao, giao cho Tran Thu Ha".
 * Ngay hien thi = createdAt.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_histories")
public class OrderHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 500)
    private String content;

    /** Nguoi thuc hien. null = he thong (dat hang, giao tu dong). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;
}
