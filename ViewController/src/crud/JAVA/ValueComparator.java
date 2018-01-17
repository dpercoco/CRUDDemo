package crud.JAVA;

import java.util.Comparator;
import java.util.Map;

final class ValueComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<?, V>> {
    public int compare(Map.Entry<?, V> o1, Map.Entry<?, V> o2) {
        return ((String) o2.getKey()).compareTo((String) o1.getKey());
    }
}
