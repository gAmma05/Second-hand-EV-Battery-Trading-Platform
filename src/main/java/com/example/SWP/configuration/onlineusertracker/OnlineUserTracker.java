package com.example.SWP.configuration.onlineusertracker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OnlineUserTracker {
    ConcurrentHashMap<Long, Instant> lastSeen = new ConcurrentHashMap<>();
    Duration timeout = Duration.ofMinutes(5);

    public void markSeen(Long userId) {
        lastSeen.put(userId, Instant.now());
    }

    public int countOnline() {
        Instant cutoff = Instant.now().minus(timeout);
        return (int) lastSeen.values().stream()
                .filter(t -> t.isAfter(cutoff))
                .count();
    }

    public boolean isOnline(Long userId) {
        Instant time = lastSeen.get(userId);
        return time != null && time.isAfter(Instant.now().minus(timeout));
    }
}
