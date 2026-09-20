package com.firstClubAssignment.membershipProgram.model;

import java.util.List;

public record PlanAndTierDetailsResponse(
        List<MembershipPlanResponse> plans,
        List<TierBenefitResponse> tierBenefits
) {}
