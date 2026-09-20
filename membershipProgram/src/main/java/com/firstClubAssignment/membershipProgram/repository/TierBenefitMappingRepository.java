package com.firstClubAssignment.membershipProgram.repository;

import com.firstClubAssignment.membershipProgram.entity.TierBenefitMapping;
import com.firstClubAssignment.membershipProgram.model.TierLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface TierBenefitMappingRepository extends JpaRepository<TierBenefitMapping, Long> {
    List<TierBenefitMapping> findByTier(TierLevel tier);
    Optional<TierBenefitMapping> findByTierAndBenefitCode(TierLevel tier, String benefitCode);
    void deleteByTierAndBenefitId(TierLevel tier, Long benefitId);
}
