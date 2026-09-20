package com.firstClubAssignment.membershipProgram.model;

public record BenefitConfig(
        boolean freeDelivery,
        double extraDiscountPercentage,
        boolean earlyAccessToSales,
        boolean prioritySupport
) {}
