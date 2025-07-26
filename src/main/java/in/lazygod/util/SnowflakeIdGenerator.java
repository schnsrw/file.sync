package in.lazygod.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {
    private final long nodeId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final long NODE_ID_BITS = 10L;
    private static final long MAX_NODE_ID = ~(-1L << NODE_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;

    private static final long NODE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + NODE_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long EPOCH = 1609459200000L; // 2021-01-01

    public SnowflakeIdGenerator(@Value("${snowflake.node-id:1}") long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException("nodeId must be between 0 and " + MAX_NODE_ID);
        }
        this.nodeId = nodeId;
    }

    public synchronized String nextId() {
        long currentTimestamp = timestamp();
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards");
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        long id = ((currentTimestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (nodeId << NODE_ID_SHIFT)
                | sequence;
        return Long.toUnsignedString(id);
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    private long timestamp() {
        return System.currentTimeMillis();
    }
}
