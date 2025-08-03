package in.lazygod.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a shared {@link ObjectMapper} instance to avoid
 * repeated allocations across the codebase.
 */
public final class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }
}

