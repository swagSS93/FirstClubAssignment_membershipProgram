package com.firstClubAssignment.membershipProgram.model;

import java.time.Instant;

public record SubscriptionResponse(
        Long userId,
        String planName,
        String tierName,
        Instant startDate,
        Instant expiryDate
) {}
