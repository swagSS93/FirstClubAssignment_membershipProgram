package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.Benefit;
import com.firstClubAssignment.membershipProgram.entity.TierBenefitMapping;
import com.firstClubAssignment.membershipProgram.model.TierLevel;
import com.firstClubAssignment.membershipProgram.repository.TierBenefitMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicBenefitEvaluatorTest {

    @Mock
    private TierBenefitMappingRepository mappingRepository;

    @InjectMocks
    private DynamicBenefitEvaluator evaluator;

    @Test
    void getBenefitsMapForTier_ReturnsMappedValues() {
        Benefit b1 = Benefit.builder().code("DISCOUNT").build();
        Benefit b2 = Benefit.builder().code("FREE_SHIPPING").build();

        TierBenefitMapping m1 = TierBenefitMapping.builder().benefit(b1).value("15%").build();
        TierBenefitMapping m2 = TierBenefitMapping.builder().benefit(b2).value("true").build();

        when(mappingRepository.findByTier(TierLevel.GOLD)).thenReturn(List.of(m1, m2));

        Map<String, String> benefits = evaluator.getBenefitsMapForTier(TierLevel.GOLD);

        assertThat(benefits)
                .hasSize(2)
                .containsEntry("DISCOUNT", "15%")
                .containsEntry("FREE_SHIPPING", "true");
    }

    @Test
    void getDiscountPercentage_Present_ReturnsValue() {
        TierBenefitMapping mapping = TierBenefitMapping.builder().value("12.5").build();
        when(mappingRepository.findByTierAndBenefitCode(TierLevel.PLATINUM, "EXTRA_DISCOUNT_PERCENTAGE"))
                .thenReturn(Optional.of(mapping));

        double discount = evaluator.getDiscountPercentage(TierLevel.PLATINUM);

        assertThat(discount).isEqualTo(12.5);
    }

    @Test
    void getDiscountPercentage_NotPresent_ReturnsZero() {
        when(mappingRepository.findByTierAndBenefitCode(TierLevel.SILVER, "EXTRA_DISCOUNT_PERCENTAGE"))
                .thenReturn(Optional.empty());

        double discount = evaluator.getDiscountPercentage(TierLevel.SILVER);

        assertThat(discount).isEqualTo(0.0);
    }

    @Test
    void hasFreeDelivery_Present_ReturnsTrue() {
        TierBenefitMapping mapping = TierBenefitMapping.builder().value("true").build();
        when(mappingRepository.findByTierAndBenefitCode(TierLevel.GOLD, "FREE_DELIVERY"))
                .thenReturn(Optional.of(mapping));

        boolean freeDelivery = evaluator.hasFreeDelivery(TierLevel.GOLD);

        assertThat(freeDelivery).isTrue();
    }
}