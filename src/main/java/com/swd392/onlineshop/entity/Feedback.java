package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.Gender;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Feedback chung hoac cho 1 san pham cu the.
 * - Public: gui tu menu header, nut Feedback o Products List / Product Details,
 *   Order Information, hoac link trong email gui khi don SUCCESS.
 * - Marketing: Feedbacks List / Feedback Details (xem, doi status show/hide).
 * - Admin Dashboard: rating trung binh (tong, theo product category).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedbacks")
public class Feedback extends BaseEntity {

    /** Contact theo email nguoi gui (tao moi neu chua co) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** null = feedback chung cho shop */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ===== Contact information luc gui feedback =====
    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String mobile;

    /** 1..5 sao */
    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String content;

    /** Status: Show/Hide */
    @Column(nullable = false)
    private boolean visible = true;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackImage> images = new ArrayList<>();
}
