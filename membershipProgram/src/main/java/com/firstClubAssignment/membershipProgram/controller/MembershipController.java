package com.firstClubAssignment.membershipProgram.controller;

import com.firstClubAssignment.membershipProgram.model.*;
import com.firstClubAssignment.membershipProgram.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
@Tag(name = "Membership Management API", description = "Endpoints for managing user plans, subscriptions, orders, and admin benefits")
@SecurityRequirement(name = "bearerAuth")
public class MembershipController {

    private final MembershipService membershipService;

    // ==========================================
    // USER ENDPOINTS
    // ==========================================

    @GetMapping("/plans-and-tiers")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get plans and tier details", description = "Retrieves all available membership plans along with dynamic tier benefits.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved plan and tier details")
    public ResponseEntity<PlanAndTierDetailsResponse> getPlansAndTiers() {
        return ResponseEntity.ok(membershipService.getAllPlansAndTiers());
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Subscribe user", description = "Subscribes the authenticated user to a membership plan and tier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscribed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid plan or tier provided"),
            @ApiResponse(responseCode = "404", description = "User or plan not found")
    })
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestBody PlanAndTierRequest request) {
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(membershipService.subscribe(email, request));
    }

    @GetMapping("/user/subscribedPlan")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get subscribed user's plan and tier details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's plan and tier details"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserPlanAndTierDetailsResponse> getSubscribedUserPlanAndTier(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(membershipService.getUserPlanAndTierDetails(email));
    }

    @PutMapping("/subscription")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update subscription", description = "Upgrades or downgrades the user's active membership plan or tier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid plan or no active subscription found")
    })
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestBody PlanAndTierRequest request) {
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(membershipService.updateSubscription(email, request));
    }

    @PostMapping("/subscription/cancel")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Cancel subscription", description = "Cancels the user's active membership subscription.")
    public ResponseEntity<String> cancelSubscription(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        membershipService.cancelSubscription(email);
        return ResponseEntity.ok("Subscription cancelled successfully.");
    }

    @PostMapping("/order")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Place order")
    public ResponseEntity<OrderResponse> placeOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestBody PlaceOrderRequest request) {
        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(membershipService.placeOrder(email, request));
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    @PostMapping("/admin/plans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add membership plan")
    public ResponseEntity<MembershipPlanResponse> addPlan(@RequestBody MembershipPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.addPlan(request));
    }

    @PostMapping("/admin/benefits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create benefit type")
    public ResponseEntity<BenefitResponse> createBenefit(@RequestBody CreateBenefitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createBenefit(request));
    }

    @PostMapping("/admin/benefits/tag")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tag benefit to tier")
    public ResponseEntity<TierBenefitDetailResponse> tagBenefitToTier(@RequestBody TagBenefitToTierRequest request) {
        return ResponseEntity.ok(membershipService.tagBenefitToTier(request));
    }

    @PutMapping("/admin/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update plan")
    public ResponseEntity<MembershipPlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(membershipService.updatePlan(id, request));
    }

    @GetMapping("/admin/benefits/{tierName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get benefits for tier")
    public ResponseEntity<List<TierBenefitDetailResponse>> getBenefitsForTier(@PathVariable String tierName) {
        return ResponseEntity.ok(membershipService.getBenefitsForTier(tierName));
    }

    @DeleteMapping("/admin/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete plan")
    public ResponseEntity<String> deletePlan(@PathVariable Long id) {
        membershipService.deletePlan(id);
        return ResponseEntity.ok("Plan deleted successfully.");
    }

    @DeleteMapping("/admin/benefits/{tierName}/{benefitId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove benefit from tier")
    public ResponseEntity<String> deleteBenefit(
            @PathVariable String tierName,
            @PathVariable Long benefitId) {
        membershipService.removeBenefitFromTier(tierName, benefitId);
        return ResponseEntity.ok("Benefit unlinked from tier successfully.");
    }
}