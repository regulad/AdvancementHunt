package quest.ender.AdvancementHunt.util;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class TimeUtil {
    public static @NotNull Instant instantInFuture(long distance) {
        return Instant.ofEpochMilli(System.currentTimeMillis() + distance);
    }
}
