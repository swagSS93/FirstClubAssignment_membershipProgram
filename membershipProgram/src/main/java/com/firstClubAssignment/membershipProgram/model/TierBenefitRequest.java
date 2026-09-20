package com.firstClubAssignment.membershipProgram.model;

import java.util.List;

public record TierBenefitRequest(String tierName, List<String> benefits) {}
