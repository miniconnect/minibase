package hu.webarticum.minibase.storage.impl.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public final class DiffTableUtil {

    private DiffTableUtil() {
        // utility class
    }


    public static <T> ImmutableList<T> mergeUnique(
            ImmutableList<T> existingValues, Iterable<T> newValues, Comparator<T> comparator) {
        SortedSet<T> insertedValues = new TreeSet<>();
        Map<Integer, SortedSet<T>> insertions = new HashMap<>();
        for (T value : newValues) {
            if (insertedValues.contains(value)) {
                continue;
            }
            int position = existingValues.binarySearch(value, comparator);
            if (position < 0) {
                int insertPosition = -1 - position;
                SortedSet<T> insertionsAtPosition = insertions.computeIfAbsent(insertPosition, k -> new TreeSet<>());
                insertionsAtPosition.add(value);
                insertedValues.add(value);
            }
        }
        int existingSize = existingValues.size();
        int newSize = existingSize + insertedValues.size();
        List<T> resultBuilder = new ArrayList<>(newSize);
        SortedSet<T> beforeValues = insertions.get(0);
        if (beforeValues != null) {
            resultBuilder.addAll(beforeValues);
        }
        for (int i = 0; i < existingSize; i++) {
            resultBuilder.add(existingValues.get(i));
            SortedSet<T> nextValues = insertions.get(i + 1);
            if (nextValues != null) {
                resultBuilder.addAll(nextValues);
            }
        }
        return ImmutableList.fromCollection(resultBuilder);

    }

    static LargeInteger adjustByDeletions(
            NavigableSet<LargeInteger> deletions, LargeInteger start, LargeInteger count) {
        LargeInteger targetPosition;

        LargeInteger position = start;
        LargeInteger remaining = count;
        do {
            targetPosition = position.add(remaining);
            Collection<LargeInteger> foundItems = deletions.subSet(position, targetPosition);
            remaining = LargeInteger.of(foundItems.size());
            position = targetPosition;
        } while (!remaining.equals(LargeInteger.ZERO));

        while (deletions.contains(targetPosition)) {
            targetPosition = targetPosition.add(LargeInteger.ONE);
        }

        return targetPosition;
    }

    static LargeInteger deadjustByDeletions(
            NavigableSet<LargeInteger> deletions, LargeInteger baseRowIndex) {
        Collection<LargeInteger> subDeletions = deletions.subSet(LargeInteger.ZERO, baseRowIndex);
        LargeInteger deletionCount = LargeInteger.of(subDeletions.size());
        return baseRowIndex.subtract(deletionCount);
    }

}
