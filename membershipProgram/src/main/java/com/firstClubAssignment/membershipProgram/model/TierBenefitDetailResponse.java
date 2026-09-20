package com.firstClubAssignment.membershipProgram.model;

import com.firstClubAssignment.membershipProgram.entity.Benefit;

public record TierBenefitDetailResponse(
        String tierName,
        String benefitCode,
        String benefitName,
        String value,
        Benefit.BenefitType type
) {}
