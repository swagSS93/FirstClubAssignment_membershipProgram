package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.model.UserActivityContext;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class OrderEventPublisher {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final List<Consumer<UserActivityContext>> listeners = new CopyOnWriteArrayList<>();

    public void registerListener(Consumer<UserActivityContext> listener) {
        listeners.add(listener);
    }

    public void publishOrderPlacedEvent(UserActivityContext context) {
        executor.submit(() -> listeners.forEach(listener -> listener.accept(context)));
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
