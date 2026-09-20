package com.firstClubAssignment.membershipProgram.strategy;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.model.UserActivityContext;

public class OrderValueStrategy implements QualificationStrategy {
    private final double requiredSpend;

    public OrderValueStrategy(double requiredSpend) {
        this.requiredSpend = requiredSpend;
    }

    @Override
    public boolean isEligible(UserActivityContext context, TierLevel targetTier) {
        return context.monthlyOrderValue() >= requiredSpend;
    }
}
