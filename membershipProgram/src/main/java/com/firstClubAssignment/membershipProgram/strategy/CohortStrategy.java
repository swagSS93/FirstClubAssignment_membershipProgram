package com.firstClubAssignment.membershipProgram.strategy;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.model.UserActivityContext;

public class CohortStrategy implements QualificationStrategy {
    private final String targetCohortId;

    public CohortStrategy(String targetCohortId) {
        this.targetCohortId = targetCohortId;
    }

    @Override
    public boolean isEligible(UserActivityContext context, TierLevel targetTier) {
        return context.cohort() != null && targetCohortId.equalsIgnoreCase(context.cohort().cohortId());
    }
}
