package com.firstClubAssignment.membershipProgram.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "membership_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., MONTHLY, QUARTERLY, YEARLY

    private Double price;
    private Integer durationInMonths;
}
