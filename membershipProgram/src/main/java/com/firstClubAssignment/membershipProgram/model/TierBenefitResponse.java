package com.firstClubAssignment.membershipProgram.model;

import java.util.List;

public record TierBenefitResponse(String tierName, List<TierBenefitDetailResponse> benefits) {}