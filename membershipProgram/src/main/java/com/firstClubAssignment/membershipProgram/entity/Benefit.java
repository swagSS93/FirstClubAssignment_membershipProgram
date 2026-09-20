package com.firstClubAssignment.membershipProgram.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "benefits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "FREE_DELIVERY", "EXTRA_DISCOUNT_PERCENTAGE", "PRIORITY_SUPPORT"

    @Column(nullable = false)
    private String name; // e.g., "Extra Discount Percentage"

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType type; // BOOLEAN, NUMERIC, STRING

    public enum BenefitType {
        BOOLEAN, NUMERIC, STRING
    }
}
