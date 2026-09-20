package com.firstClubAssignment.membershipProgram.strategy;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.model.UserActivityContext;

@FunctionalInterface
public interface QualificationStrategy {
    boolean isEligible(UserActivityContext context, TierLevel targetTier);
}
