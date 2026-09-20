package com.firstClubAssignment.membershipProgram.strategy;

import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.model.UserActivityContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TierEvaluationEngine {
    private final List<QualificationStrategy> platinumStrategies = List.of(
            new OrderValueStrategy(1000.0),
            new OrderCountStrategy(15),
            new CohortStrategy("VIP_COHORT")
    );

    private final List<QualificationStrategy> goldStrategies = List.of(
            new OrderValueStrategy(500.0),
            new OrderCountStrategy(5)
    );

    public TierLevel evaluateTier(UserActivityContext context) {
        if (platinumStrategies.stream().anyMatch(s -> s.isEligible(context, TierLevel.PLATINUM))) {
            return TierLevel.PLATINUM;
        }
        if (goldStrategies.stream().anyMatch(s -> s.isEligible(context, TierLevel.GOLD))) {
            return TierLevel.GOLD;
        }
        return TierLevel.SILVER;
    }
}
