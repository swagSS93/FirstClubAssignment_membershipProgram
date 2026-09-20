package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.*;
import com.firstClubAssignment.membershipProgram.exception.InvalidRequestException;
import com.firstClubAssignment.membershipProgram.exception.ResourceNotFoundException;
import com.firstClubAssignment.membershipProgram.model.*;
import com.firstClubAssignment.membershipProgram.repository.*;
import com.firstClubAssignment.membershipProgram.strategy.TierEvaluationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MembershipPlanRepository planRepository;
    @Mock
    private BenefitRepository benefitRepository;
    @Mock
    private TierBenefitMappingRepository tierBenefitMappingRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TierEvaluationEngine tierEngine;
    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private MembershipService membershipService;

    private User testUser;
    private MembershipPlan monthlyPlan;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("John Doe")
                .tier("SILVER")
                .build();

        monthlyPlan = MembershipPlan.builder()
                .id(10L)
                .name("MONTHLY")
                .price(9.99)
                .durationInMonths(1)
                .build();
    }

    // ==========================================
    // USER OPERATIONS TESTS
    // ==========================================

    @Test
    void getAllPlansAndTiers_Success() {
        Benefit benefit = Benefit.builder().code("FREE_DELIVERY").name("Free Delivery").type(Benefit.BenefitType.valueOf("BOOLEAN")).build();
        TierBenefitMapping mapping = TierBenefitMapping.builder()
                .tier(TierLevel.SILVER)
                .benefit(benefit)
                .value("true")
                .build();

        when(planRepository.findAll()).thenReturn(List.of(monthlyPlan));
        when(tierBenefitMappingRepository.findAll()).thenReturn(List.of(mapping));

        PlanAndTierDetailsResponse response = membershipService.getAllPlansAndTiers();

        assertThat(response.plans()).hasSize(1);
        assertThat(response.plans().get(0).name()).isEqualTo("MONTHLY");
        assertThat(response.tierBenefits()).hasSize(1);
        assertThat(response.tierBenefits().get(0).tierName()).isEqualTo("SILVER");
    }

    @Test
    void getUserPlanAndTierDetails_Success() {
        testUser.setMembershipPlan(monthlyPlan);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(tierBenefitMappingRepository.findAll()).thenReturn(List.of());

        UserPlanAndTierDetailsResponse response = membershipService.getUserPlanAndTierDetails("user@example.com");

        assertThat(response.userEmail()).isEqualTo("user@example.com");
        assertThat(response.currentPlan().name()).isEqualTo("MONTHLY");
        assertThat(response.userTier()).isEqualTo("SILVER");
    }

    @Test
    void subscribe_Success() {
        PlanAndTierRequest request = new PlanAndTierRequest("MONTHLY");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(planRepository.findByNameIgnoreCase("MONTHLY")).thenReturn(Optional.of(monthlyPlan));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionResponse response = membershipService.subscribe("user@example.com", request);

        assertThat(response.planName()).isEqualTo("MONTHLY");
        assertThat(response.startDate()).isNotNull();
        assertThat(response.expiryDate()).isAfter(response.startDate());
        verify(userRepository).save(testUser);
    }

    @Test
    void subscribe_InvalidPlanType_ThrowsException() {
        PlanAndTierRequest request = new PlanAndTierRequest("INVALID_PLAN");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> membershipService.subscribe("user@example.com", request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid PlanType");
    }

    @Test
    void updateSubscription_Success() {
        testUser.setMembershipPlan(monthlyPlan);
        testUser.setMembershipExpiryDate(Instant.now().plus(10, ChronoUnit.DAYS));

        PlanAndTierRequest request = new PlanAndTierRequest("YEARLY");
        MembershipPlan yearlyPlan = MembershipPlan.builder().id(11L).name("YEARLY").durationInMonths(12).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(planRepository.findByNameIgnoreCase("YEARLY")).thenReturn(Optional.of(yearlyPlan));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionResponse response = membershipService.updateSubscription("user@example.com", request);

        assertThat(response.planName()).isEqualTo("YEARLY");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateSubscription_NoActiveSubscription_ThrowsException() {
        PlanAndTierRequest request = new PlanAndTierRequest("MONTHLY");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> membershipService.updateSubscription("user@example.com", request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User does not have an active subscription to update.");
    }

    @Test
    void cancelSubscription_Success() {
        testUser.setMembershipPlan(monthlyPlan);
        testUser.setTier("SILVER");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        membershipService.cancelSubscription("user@example.com");

        assertThat(testUser.getMembershipPlan()).isNull();
        assertThat(testUser.getTier()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void placeOrder_Success() {
        PlaceOrderRequest request = new PlaceOrderRequest(100.0);
        Order savedOrder = Order.builder().orderId(101L).userId(1L).totalPrice(100.0).purchaseDate(Instant.now()).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(savedOrder));

        OrderResponse response = membershipService.placeOrder("user@example.com", request);

        assertThat(response.orderId()).isEqualTo(101L);
        assertThat(response.totalPrice()).isEqualTo(100.0);
        verify(eventPublisher).publishOrderPlacedEvent(any(UserActivityContext.class));
    }

    @Test
    void onUserOrderActivity_UpgradesTier() {
        UserActivityContext context = new UserActivityContext(1L, "user@example.com", 5, 500.0, new UserCohort("VIP_COHORT", "VIP"));
        testUser.setMembershipPlan(monthlyPlan);
        testUser.setMembershipExpiryDate(Instant.now().plus(10, ChronoUnit.DAYS));
        testUser.setTier("SILVER");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(tierEngine.evaluateTier(context)).thenReturn(TierLevel.GOLD);

        membershipService.onUserOrderActivity(context);

        assertThat(testUser.getTier()).isEqualTo("GOLD");
        verify(userRepository).save(testUser);
    }

    // ==========================================
    // ADMIN OPERATIONS TESTS
    // ==========================================

    @Test
    void addPlan_Success() {
        MembershipPlanRequest request = new MembershipPlanRequest("MONTHLY", 10.0, 1);
        when(planRepository.save(any(MembershipPlan.class))).thenReturn(monthlyPlan);

        MembershipPlanResponse response = membershipService.addPlan(request);

        assertThat(response.name()).isEqualTo("MONTHLY");
        verify(planRepository).save(any(MembershipPlan.class));
    }

    @Test
    void deletePlan_NotFound_ThrowsException() {
        when(planRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> membershipService.deletePlan(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void tagBenefitToTier_Success() {
        TagBenefitToTierRequest request = new TagBenefitToTierRequest("GOLD", "DISCOUNT10", "10");
        Benefit benefit = Benefit.builder().id(1L).code("DISCOUNT10").name("10% Off").build();

        when(benefitRepository.findByCodeIgnoreCase("DISCOUNT10")).thenReturn(Optional.of(benefit));
        when(tierBenefitMappingRepository.findByTierAndBenefitCode(TierLevel.GOLD, "DISCOUNT10")).thenReturn(Optional.empty());
        when(tierBenefitMappingRepository.save(any(TierBenefitMapping.class))).thenAnswer(i -> i.getArgument(0));

        TierBenefitDetailResponse response = membershipService.tagBenefitToTier(request);

        assertThat(response.tierName()).isEqualTo("GOLD");
        assertThat(response.benefitCode()).isEqualTo("DISCOUNT10");
        assertThat(response.value()).isEqualTo("10");
    }
}
