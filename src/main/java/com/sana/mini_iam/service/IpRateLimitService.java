package com.sana.mini_iam.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpRateLimitService {

    // Maximal 20 Login-Anfragen
    private static final int MAX_REQUESTS = 20;

    // Innerhalb von 60 Sekunden
    private static final int WINDOW_SECONDS = 60;

    // Speichert die Zeitpunkte der Login-Anfragen pro IP
    private final Map<String, Deque<Instant>> requests =
            new ConcurrentHashMap<>();

    public boolean isAllowed(String ipAddress) {

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(WINDOW_SECONDS);

        Deque<Instant> timestamps = requests.computeIfAbsent(
                ipAddress,
                key -> new ArrayDeque<>()
        );

        // Synchronisierung verhindert gleichzeitige Änderungen
        // an der Anfrageliste derselben IP.
        synchronized (timestamps) {

            // Alte Anfragen entfernen
            while (!timestamps.isEmpty()
                    && !timestamps.peekFirst().isAfter(windowStart)) {

                timestamps.removeFirst();
            }

            // Limit erreicht?
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }

            // Aktuelle Anfrage speichern
            timestamps.addLast(now);

            return true;
        }
    }
}