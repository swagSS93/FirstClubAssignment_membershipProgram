package com.firstClubAssignment.membershipProgram.repository;

import com.firstClubAssignment.membershipProgram.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {
    Optional<Benefit> findByCodeIgnoreCase(String code);
}
