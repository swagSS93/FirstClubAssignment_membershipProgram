package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.model.UserActivityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventPublisherTest {

    private OrderEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OrderEventPublisher();
    }

    @AfterEach
    void tearDown() {
        publisher.shutdown();
    }

    @Test
    void publishOrderPlacedEvent_TriggersRegisteredListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        UserActivityContext context = new UserActivityContext(1L, "user@example.com", 2, 200.0, null);

        publisher.registerListener(ctx -> {
            if (ctx.userEmail().equals("user@example.com")) {
                listenerCalled.set(true);
            }
            latch.countDown();
        });

        publisher.publishOrderPlacedEvent(context);

        boolean completed = latch.await(2, TimeUnit.SECONDS);

        assertThat(completed).isTrue();
        assertThat(listenerCalled.get()).isTrue();
    }
}