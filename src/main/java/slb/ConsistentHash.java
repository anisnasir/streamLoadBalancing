package slb;

import java.util.Collection;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

// CONTRACT: keys must implement toString()
public class ConsistentHash<T> {
    private final int numberOfReplicas;
    private final SortedMap<Integer, T> circle = new TreeMap<Integer, T>();
    private final Random rnd = new Random(123456789);

    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            add(node);
        }
    }

    public void add(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = rnd.nextInt();
            circle.put(hash, node);
        }
    }

//    public void remove(T node) {
//        for (int i = 0; i < numberOfReplicas; i++) {
//            circle.remove((node.hashCode() * 17 + i));
//        }
//    }

    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = key.toString().hashCode();
        if (!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

}
