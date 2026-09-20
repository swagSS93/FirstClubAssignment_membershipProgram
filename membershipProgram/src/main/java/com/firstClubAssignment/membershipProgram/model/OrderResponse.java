package com.firstClubAssignment.membershipProgram.model;

import java.time.Instant;

public record OrderResponse(Long orderId, Long userId, Instant purchaseDate, Double totalPrice) {}
