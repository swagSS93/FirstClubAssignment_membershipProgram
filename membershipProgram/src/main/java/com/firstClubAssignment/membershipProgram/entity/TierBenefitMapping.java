package com.firstClubAssignment.membershipProgram.entity;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_benefit_mappings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tier", "benefit_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierBenefitMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TierLevel tier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit;

    @Column(name = "benefit_value", nullable = false)
    private String value; // e.g., "true", "10.0", "FAST_2_DAY"
}
