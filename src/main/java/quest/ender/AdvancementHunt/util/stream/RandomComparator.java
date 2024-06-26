package quest.ender.AdvancementHunt.util.stream;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

/**
 * <a href="https://stackoverflow.com/questions/20058366/shuffle-a-list-of-integers-with-java-8-streams-api">...</a>
 *
 * @param <T>
 */
public final class RandomComparator<T> implements Comparator<T> {
    private final Map<T, Integer> map = new IdentityHashMap<>();
    private final Random random;

    public RandomComparator() {
        this(new Random());
    }

    public RandomComparator(Random random) {
        this.random = random;
    }

    @Override
    public int compare(T t1, T t2) {
        return Integer.compare(valueFor(t1), valueFor(t2));
    }

    private int valueFor(T t) {
        synchronized (map) {
            return map.computeIfAbsent(t, ignore -> random.nextInt());
        }
    }

}
