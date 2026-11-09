package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.CustomerStatus;
import com.swd392.onlineshop.enums.Gender;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer / contact, dinh danh bang email (Marketing: Customers List, Customer Details).
 * Gom ca nguoi chua co tai khoan (Guest dat hang, gui feedback, Marketing them tay).
 * <p>
 * Duoc tao/cap nhat khi:
 * - Cart Completion: thong tin nguoi nhan (name, email, mobile, gender) moi hoac khac
 *   -> luu thong tin moi nhat + ghi CustomerHistory.
 * - Gui Feedback: tao contact neu email chua co.
 * - Xac thuc email sau Register: tao/lien ket voi User cung email.
 * - Marketing Add/Edit (tru status) -> ghi CustomerHistory.
 * <p>
 * status do he thong tinh lai (xem CustomerStatus).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    /** Ma tren danh sach, vi du KH0001. */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String mobile;

    @Column(length = 255)
    private String address;

    /** Ghi chu cua Marketing, khong phai ghi chu don hang */
    @Column(length = 500)
    private String note;

    /** Hien/An ho so tren man Khach hang. Khac voi status vong doi. */
    @Column(nullable = false)
    private boolean visible = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.CONTACT;

    /** Tai khoan cung email, null neu chua dang ky. 1 User chi lien ket 1 Customer */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<CustomerHistory> histories = new ArrayList<>();
}
