package com.firstClubAssignment.membershipProgram.model;

public record TagBenefitToTierRequest(
        String tierName,
        String benefitCode,
        String value
) {}
