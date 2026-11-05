package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.enums.OrderStatus;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Don hang. Ngay dat hang = createdAt.
 * <p>
 * Cart Completion (submit), lam trong 1 transaction:
 * status = SUBMITTED -> tru ton kho -> giao xoay vong cho Sale ACTIVE
 * -> luu/cap nhat contact nguoi nhan (Customer + CustomerHistory) -> gui email xac nhan
 * + huong dan thanh toan (thong tin ngan hang lay tu config file).
 * <p>
 * - Customer: My Orders, Order Information (Re-buy, Feedback tung san pham).
 *   Khi SUBMITTED: Update (nap lai items vao gio -> Cart Details -> submit lai don nay) hoac Cancel.
 * - Sale: Orders List / Order Details cua don duoc giao; doi status + saleNote.
 * - Sale Manager: xem tat ca don, doi status + saleNote, giao lai cho Sale khac.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders") // "order" la tu khoa SQL
public class Order extends BaseEntity {

    /**
     * Contact cua nguoi dat: user da login -> Customer lien ket voi User do;
     * Guest -> Customer theo email nguoi nhan.
     * My Orders = cac don co customer.user = user dang login.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // ===== Receiver information (Cart Contact), luu lai tai thoi diem dat =====
    @Column(nullable = false, length = 100)
    private String receiverName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender receiverGender;

    @Column(nullable = false, length = 100)
    private String receiverEmail;

    @Column(nullable = false, length = 15)
    private String receiverMobile;

    @Column(nullable = false, length = 255)
    private String receiverAddress;

    @Column(length = 500)
    private String note;

    // ===== Order data =====
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.SUBMITTED;

    /**
     * Sale phu trach. Khi submit: tu dong giao xoay vong cho Sale ACTIVE
     * (Sale co don duoc giao gan nhat cu nhat). Sale Manager co the giao lai.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sale_id")
    private User assignedSale;

    /** Ghi chu noi bo cua Sale / Sale Manager */
    @Column(length = 500)
    private String saleNote;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
