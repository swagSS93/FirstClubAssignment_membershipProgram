package com.firstClubAssignment.membershipProgram.model;

import java.util.List;

public record UserPlanAndTierDetailsResponse(
        String userEmail,
        MembershipPlanResponse currentPlan,
        String userTier,
        List<TierBenefitDetailResponse> tierBenefits
) {}