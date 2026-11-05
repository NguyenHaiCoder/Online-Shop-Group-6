package com.swd392.onlineshop.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Gio hang cua user da dang nhap: giu qua cac phien duyet web cho den khi submit don.
 * - Guest: gio luu trong HttpSession (mat khi dong trinh duyet, khong co entity).
 *   Khi login, gio trong session duoc gop vao Cart nay.
 * - Khong luu gia: Cart Details/Cart Contact luon lay gia moi nhat cua Product.
 * - Submit thanh cong (Cart Completion) -> xoa het items.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
