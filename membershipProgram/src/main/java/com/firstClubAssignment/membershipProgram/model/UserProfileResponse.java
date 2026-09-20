package com.firstClubAssignment.membershipProgram.model;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String phoneNo,
        String email,
        String membershipPlanName,
        String tier,
        Instant membershipStartDate,
        Instant membershipExpiryDate
) {}
