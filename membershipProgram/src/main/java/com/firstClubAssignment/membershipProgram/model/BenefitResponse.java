package com.firstClubAssignment.membershipProgram.model;

import com.firstClubAssignment.membershipProgram.entity.Benefit;

public record BenefitResponse(
        Long id,
        String code,
        String name,
        String description,
        Benefit.BenefitType type
) {}
