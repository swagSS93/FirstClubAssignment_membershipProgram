package com.firstClubAssignment.membershipProgram.repository;

import com.firstClubAssignment.membershipProgram.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    Optional<MembershipPlan> findByNameIgnoreCase(String name);
}