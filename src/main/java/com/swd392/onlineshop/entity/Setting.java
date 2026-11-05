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
        uniqueConstraints = @UniqueConstraint(columnNames = {"type", "value"}))
public class Setting extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettingType type;

    /** Ten hien thi */
    @Column(nullable = false, length = 100)
    private String name;

    /** Ma dung trong code (vd: ADMIN, MARKETING, SALE...) */
    @Column(nullable = false, length = 100)
    private String value;

    private Integer displayOrder;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
