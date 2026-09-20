package com.firstClubAssignment.membershipProgram.model;

import com.firstClubAssignment.membershipProgram.entity.Benefit;

public record CreateBenefitRequest(
        String code,
        String name,
        String description,
        Benefit.BenefitType type
) {}
