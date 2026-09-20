package com.firstClubAssignment.membershipProgram.strategy;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.model.UserActivityContext;

public class OrderCountStrategy implements QualificationStrategy {
    private final int requiredOrders;

    public OrderCountStrategy(int requiredOrders) {
        this.requiredOrders = requiredOrders;
    }

    @Override
    public boolean isEligible(UserActivityContext context, TierLevel targetTier) {
        return context.monthlyOrderCount() >= requiredOrders;
    }
}
