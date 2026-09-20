package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.*;
import com.firstClubAssignment.membershipProgram.exception.InvalidRequestException;
import com.firstClubAssignment.membershipProgram.exception.ResourceNotFoundException;
import com.firstClubAssignment.membershipProgram.model.*;
import com.firstClubAssignment.membershipProgram.repository.*;
import com.firstClubAssignment.membershipProgram.strategy.TierEvaluationEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipPlanRepository planRepository;
    private final BenefitRepository benefitRepository;
    private final TierBenefitMappingRepository tierBenefitMappingRepository;
    private final OrderRepository orderRepository;

    private final TierEvaluationEngine tierEngine;
    private final OrderEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        eventPublisher.registerListener(this::onUserOrderActivity);
    }

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    public PlanAndTierDetailsResponse getAllPlansAndTiers() {
        List<MembershipPlanResponse> plans = planRepository.findAll().stream()
                .map(p -> new MembershipPlanResponse(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getDurationInMonths()
                ))
                .toList();

        List<TierBenefitDetailResponse> mappedBenefits = tierBenefitMappingRepository.findAll().stream()
                .map(m -> new TierBenefitDetailResponse(
                        m.getTier().name(),
                        m.getBenefit().getCode(),
                        m.getBenefit().getName(),
                        m.getValue(),
                        m.getBenefit().getType()
                ))
                .toList();

        Map<String, List<TierBenefitDetailResponse>> benefitsByTier = mappedBenefits.stream()
                .collect(Collectors.groupingBy(TierBenefitDetailResponse::tierName));

        List<TierBenefitResponse> tierBenefits = benefitsByTier.entrySet().stream()
                .map(entry -> new TierBenefitResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new PlanAndTierDetailsResponse(plans, tierBenefits);
    }

    @Transactional(readOnly = true)
    public UserPlanAndTierDetailsResponse getUserPlanAndTierDetails(String userEmail) {
        User user = getUserByEmail(userEmail);

        MembershipPlanResponse currentPlan = null;
        if (user.getMembershipPlan() != null) {
            MembershipPlan plan = user.getMembershipPlan();
            currentPlan = new MembershipPlanResponse(
                    plan.getId(),
                    plan.getName(),
                    plan.getPrice(),
                    plan.getDurationInMonths()
            );
        }

        List<TierBenefitDetailResponse> userTierBenefits = List.of();
        if (user.getTier() != null) {
            userTierBenefits = tierBenefitMappingRepository.findAll().stream()
                    .filter(m -> m.getTier().name().equalsIgnoreCase(user.getTier()))
                    .map(m -> new TierBenefitDetailResponse(
                            m.getTier().name(),
                            m.getBenefit().getCode(),
                            m.getBenefit().getName(),
                            m.getValue(),
                            m.getBenefit().getType()
                    ))
                    .toList();
        }

        return new UserPlanAndTierDetailsResponse(
                user.getEmail(),
                currentPlan,
                user.getTier(),
                userTierBenefits
        );
    }

    @Transactional
    public SubscriptionResponse subscribe(String userEmail, PlanAndTierRequest request) {
        User user = getUserByEmail(userEmail);

        PlanType planType = parsePlanType(request.planName());

        MembershipPlan plan = planRepository.findByNameIgnoreCase(planType.name())
                .orElseThrow(() -> new ResourceNotFoundException("Membership Plan not configured in DB: " + planType.name()));

        Instant now = Instant.now();
        Instant expiry = now.atZone(ZoneOffset.UTC)
                .plusMonths(planType.getDurationInMonths())
                .toInstant();

        user.setMembershipPlan(plan);
        user.setMembershipStartDate(now);
        user.setMembershipExpiryDate(expiry);

        User savedUser = userRepository.save(user);

        return new SubscriptionResponse(savedUser.getId(), plan.getName(), savedUser.getTier(), now, expiry);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(String userEmail, PlanAndTierRequest request) {
        User user = getUserByEmail(userEmail);

        if (user.getMembershipPlan() == null || isSubscriptionExpired(user)) {
            throw new InvalidRequestException("User does not have an active subscription to update.");
        }

        PlanType newPlanType = parsePlanType(request.planName());

        MembershipPlan newPlan = planRepository.findByNameIgnoreCase(newPlanType.name())
                .orElseThrow(() -> new ResourceNotFoundException("Membership Plan not configured in DB: " + newPlanType.name()));

        Instant now = Instant.now();
        Instant newExpiry = now.atZone(ZoneOffset.UTC)
                .plusMonths(newPlanType.getDurationInMonths())
                .toInstant();

        user.setMembershipPlan(newPlan);
        user.setMembershipExpiryDate(newExpiry);

        User updatedUser = userRepository.save(user);

        return new SubscriptionResponse(updatedUser.getId(), newPlan.getName(), updatedUser.getTier(), updatedUser.getMembershipStartDate(), newExpiry);
    }

    @Transactional
    public void cancelSubscription(String userEmail) {
        User user = getUserByEmail(userEmail);
        user.setMembershipPlan(null);
        user.setTier(null);
        user.setMembershipStartDate(null);
        user.setMembershipExpiryDate(null);
        userRepository.save(user);
    }

    @Transactional
    public OrderResponse placeOrder(String userEmail, PlaceOrderRequest request) {
        User user = getUserByEmail(userEmail);

        Order order = Order.builder()
                .userId(user.getId())
                .purchaseDate(Instant.now())
                .totalPrice(request.totalPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        List<Order> userOrders = orderRepository.findByUserId(user.getId());
        double totalSpend = userOrders.stream().mapToDouble(Order::getTotalPrice).sum();

        UserActivityContext context = new UserActivityContext(
                user.getId(),
                userEmail,
                userOrders.size(),
                totalSpend,
                new UserCohort("VIP_COHORT", "VIP")
        );
        eventPublisher.publishOrderPlacedEvent(context);

        return new OrderResponse(savedOrder.getOrderId(), savedOrder.getUserId(), savedOrder.getPurchaseDate(), savedOrder.getTotalPrice());
    }

    @Transactional
    public void onUserOrderActivity(UserActivityContext context) {
        userRepository.findByEmail(context.userEmail()).ifPresent(user -> {
            if (user.getMembershipPlan() != null && !isSubscriptionExpired(user)) {
                TierLevel evaluatedTier = tierEngine.evaluateTier(context);
                TierLevel currentTier = user.getTier() != null ? parseTierLevel(user.getTier()) : TierLevel.SILVER;

                if (evaluatedTier.getRank() > currentTier.getRank()) {
                    user.setTier(evaluatedTier.name());
                    userRepository.save(user);
                }
            }
        });
    }

    // ==========================================
    // ADMIN OPERATIONS
    // ==========================================

    @Transactional
    public MembershipPlanResponse addPlan(MembershipPlanRequest request) {
        PlanType planType = parsePlanType(request.name());

        MembershipPlan plan = MembershipPlan.builder()
                .name(planType.name())
                .price(request.price())
                .durationInMonths(planType.getDurationInMonths())
                .build();

        MembershipPlan saved = planRepository.save(plan);
        return new MembershipPlanResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDurationInMonths());
    }

    @Transactional
    public MembershipPlanResponse updatePlan(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        PlanType planType = parsePlanType(request.name());

        plan.setName(planType.name());
        plan.setPrice(request.price());
        plan.setDurationInMonths(planType.getDurationInMonths());

        MembershipPlan updated = planRepository.save(plan);
        return new MembershipPlanResponse(updated.getId(), updated.getName(), updated.getPrice(), updated.getDurationInMonths());
    }

    @Transactional
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Plan not found with id: " + id);
        }
        planRepository.deleteById(id);
    }

    // ==========================================
    // DYNAMIC BENEFIT ADMIN OPERATIONS
    // ==========================================

    @Transactional
    public BenefitResponse createBenefit(CreateBenefitRequest request) {
        Benefit benefit = Benefit.builder()
                .code(request.code().toUpperCase().trim())
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .build();

        Benefit saved = benefitRepository.save(benefit);
        return new BenefitResponse(saved.getId(), saved.getCode(), saved.getName(), saved.getDescription(), saved.getType());
    }

    @Transactional
    public TierBenefitDetailResponse tagBenefitToTier(TagBenefitToTierRequest request) {
        TierLevel tierLevel = parseTierLevel(request.tierName());

        Benefit benefit = benefitRepository.findByCodeIgnoreCase(request.benefitCode())
                .orElseThrow(() -> new ResourceNotFoundException("Benefit not found with code: " + request.benefitCode()));

        TierBenefitMapping mapping = tierBenefitMappingRepository.findByTierAndBenefitCode(tierLevel, benefit.getCode())
                .orElseGet(() -> TierBenefitMapping.builder()
                        .tier(tierLevel)
                        .benefit(benefit)
                        .build());

        mapping.setValue(request.value());
        TierBenefitMapping saved = tierBenefitMappingRepository.save(mapping);

        return new TierBenefitDetailResponse(
                saved.getTier().name(),
                benefit.getCode(),
                benefit.getName(),
                saved.getValue(),
                benefit.getType()
        );
    }

    public List<TierBenefitDetailResponse> getBenefitsForTier(String tierName) {
        TierLevel tier = parseTierLevel(tierName);
        return tierBenefitMappingRepository.findByTier(tier).stream()
                .map(m -> new TierBenefitDetailResponse(
                        m.getTier().name(),
                        m.getBenefit().getCode(),
                        m.getBenefit().getName(),
                        m.getValue(),
                        m.getBenefit().getType()
                ))
                .toList();
    }

    @Transactional
    public void removeBenefitFromTier(String tierName, Long benefitId) {
        TierLevel tier = parseTierLevel(tierName);
        tierBenefitMappingRepository.deleteByTierAndBenefitId(tier, benefitId);
    }

    // Helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private PlanType parsePlanType(String planName) {
        try {
            return PlanType.valueOf(planName.toUpperCase().trim());
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid PlanType: '" + planName + "'. Valid options are MONTHLY, QUARTERLY, YEARLY.");
        }
    }

    private TierLevel parseTierLevel(String tierName) {
        try {
            return TierLevel.valueOf(tierName.toUpperCase().trim());
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid TierLevel: '" + tierName + "'. Valid options are SILVER, GOLD, PLATINUM.");
        }
    }

    private boolean isSubscriptionExpired(User user) {
        return user.getMembershipExpiryDate() == null || Instant.now().isAfter(user.getMembershipExpiryDate());
    }
}