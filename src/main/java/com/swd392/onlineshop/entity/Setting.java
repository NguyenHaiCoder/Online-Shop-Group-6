package com.swd392.onlineshop.entity;

import com.swd392.onlineshop.enums.SettingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * System setting (Admin: View Setting List, Add/Edit/Show/Hide Setting).
 * Luu cac du lieu cau hinh: User Role, Post Category, Product Category, Admin Menu...
 * <p>
 * Vi du: type=USER_ROLE, name="Administrator", value="ADMIN"
 * -> Spring Security dung authority "ROLE_" + value.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "settings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "value"}),
                @UniqueConstraint(columnNames = {"type", "code"})
        })
public class Setting extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettingType type;

    /** Nhom tren man cau hinh, vi du Noi dung, Lien he. */
    @Column(length = 100)
    private String groupName;

    /** Ten hien thi */
    @Column(nullable = false, length = 100)
    private String name;

    /** Ma cau hinh, vi du HOTLINE, SHIPPING_POLICY. */
    @Column(nullable = false, length = 100)
    private String code;

    /** Gia tri tren form cau hinh. Danh sach chi hien rut gon. */
    @Column(nullable = false, length = 1000)
    private String value;

    private Integer displayOrder;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
