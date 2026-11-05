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
 * Anh dinh kem cua feedback.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback_images")
public class FeedbackImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(nullable = false, length = 255)
    private String imageUrl;
}
