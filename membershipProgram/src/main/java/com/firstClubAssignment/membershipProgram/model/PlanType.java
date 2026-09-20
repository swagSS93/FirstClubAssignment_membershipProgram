package com.firstClubAssignment.membershipProgram.model;

public enum PlanType {
    MONTHLY(1), QUARTERLY(3), YEARLY(12);

    private final int durationInMonths;
    PlanType(int durationInMonths) { this.durationInMonths = durationInMonths; }
    public int getDurationInMonths() { return durationInMonths; }
}
