package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.TierBenefitMapping;
import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.repository.TierBenefitMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DynamicBenefitEvaluator {

    private final TierBenefitMappingRepository mappingRepository;

    public Map<String, String> getBenefitsMapForTier(TierLevel tier) {
        List<TierBenefitMapping> mappings = mappingRepository.findByTier(tier);
        return mappings.stream()
                .collect(Collectors.toMap(
                        m -> m.getBenefit().getCode(),
                        TierBenefitMapping::getValue,
                        (v1, v2) -> v2
                ));
    }

    public double getDiscountPercentage(TierLevel tier) {
        return mappingRepository.findByTierAndBenefitCode(tier, "EXTRA_DISCOUNT_PERCENTAGE")
                .map(m -> Double.parseDouble(m.getValue()))
                .orElse(0.0);
    }

    public boolean hasFreeDelivery(TierLevel tier) {
        return mappingRepository.findByTierAndBenefitCode(tier, "FREE_DELIVERY")
                .map(m -> Boolean.parseBoolean(m.getValue()))
                .orElse(false);
    }
}