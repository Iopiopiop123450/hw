package task1.homework08;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ElementsFinder {
    public static <T> Set<T> getUniqueElements(Collection<T> collection) {
        return new HashSet<>(collection);
    }
}

