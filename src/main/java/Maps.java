import lombok.Value;

import java.util.HashMap;
import java.util.Map;

public class Maps {

    @Value
    public static class Pair<L,R> {
        private final L left;
        private final R right;

        public static <L,R> Pair<L,R> of(L l, R r) {
            return new Pair<>(l, r);
        }
    }

    public static <K,V> Map<K,V> asMap(Pair<K,V>... pairs) {
        Map<K,V> result = new HashMap<>();
        for (int x = 0; x < pairs.length; ++x) {
            result.put(pairs[x].getLeft(), pairs[x].getRight());
        }
        return result;
    }
}
