package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lich su thay doi contact, hien thanh bang o Customer Details:
 * email, full name, gender, mobile, address, updated by, updated date.
 * <p>
 * Moi dong la thong tin SAU khi thay doi (dong dau tien = luc tao contact).
 * Updated date = createdAt.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_histories")
public class CustomerHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 15)
    private String mobile;

    @Column(length = 255)
    private String address;

    /** Marketing da sua; null = he thong (khach tu cap nhat khi dat hang/gui feedback) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
