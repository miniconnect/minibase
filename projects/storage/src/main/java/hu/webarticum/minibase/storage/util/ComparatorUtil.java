package hu.webarticum.minibase.storage.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex.SortMode;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator.MultiComparatorBuilder;
import hu.webarticum.miniconnect.lang.ImmutableList;

public final class ComparatorUtil {

    private ComparatorUtil() {
        // utility class
    }
    

    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> createDefaultComparatorFor(Class<T> clazz) {
        if (clazz == String.class) {
            return (Comparator<T>) Collator.getInstance(Locale.US);
        } else {
            return (Comparator<T>) Comparator.naturalOrder();
        }
    }
    
    public static MultiComparator createMultiComparator(
            Table table,
            ImmutableList<String> columnNames,
            ImmutableList<SortMode> sortModes) {
        int size = columnNames.size();
        ImmutableList<SortMode> extendedSortModes = sortModes.resize(size, i -> SortMode.UNSORTED);
        NamedResourceStore<Column> columns = table.columns();
        MultiComparatorBuilder builder = MultiComparator.builder();
        for (int i = 0; i < size; i++) {
            String columnName = columnNames.get(i);
            SortMode sortMode = extendedSortModes.get(i);
            ColumnDefinition columnDefinition = columns.get(columnName).definition();
            Comparator<?> columnComparator = columnDefinition.comparator();
            boolean nullable = columnDefinition.isNullable();
            builder.add(columnComparator, nullable, sortMode.isAsc(), sortMode.isNullsFirst());
        }
        return builder.build();
    }
    
}
