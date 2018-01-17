package crud.JAVA;

import java.util.Comparator;
import java.util.Map;

final class ShoppingItemGroupsComparator<V extends Comparable<? super V>> implements Comparator<Map.Entry<String, Map<String, Ingredient>>> {
    public int compare(Map.Entry<String, Map<String, Ingredient>> o1, Map.Entry<String, Map<String, Ingredient>> o2) {
        return ((String) o1.getKey()).compareTo((String) o2.getKey());
    }
}
