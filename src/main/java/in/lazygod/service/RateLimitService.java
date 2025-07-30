package in.lazygod.service;

import in.lazygod.config.RateLimitProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();
    private final RateLimitProperties properties;

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean isAllowed(String key) {
        if (!properties.isEnabled()) {
            return true;
        }
        RequestCounter counter = counters.computeIfAbsent(key, k -> new RequestCounter());
        synchronized (counter) {
            long now = System.currentTimeMillis();
            if (now - counter.timestamp > 60_000) {
                counter.timestamp = now;
                counter.count = 1;
                return true;
            }
            if (counter.count >= properties.getLimit()) {
                return false;
            }
            counter.count++;
            return true;
        }
    }

    private static class RequestCounter {
        long timestamp = System.currentTimeMillis();
        int count = 0;
    }
}
