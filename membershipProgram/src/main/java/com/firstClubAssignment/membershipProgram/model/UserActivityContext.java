package com.firstClubAssignment.membershipProgram.model;

public record UserActivityContext(
        Long userId,
        String userEmail,
        int monthlyOrderCount,
        double monthlyOrderValue,
        UserCohort cohort
) {}
