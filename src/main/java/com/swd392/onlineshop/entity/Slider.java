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
 * Slider/banner tren Home Page.
 * Marketing: View Slider List, Add/Edit/Show/Hide Slider.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sliders")
public class Slider extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String image;

    /** Link khi click vao slider */
    @Column(length = 255)
    private String backlink;

    @Column(length = 500)
    private String notes;

    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    /** Show/Hide Slider */
    @Column(nullable = false)
    private boolean visible = true;
}
