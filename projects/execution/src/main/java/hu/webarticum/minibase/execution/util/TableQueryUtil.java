package hu.webarticum.minibase.execution.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.execution.impl.select.IncompatibleFiltersException;
import hu.webarticum.minibase.execution.impl.select.OrderByEntry;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.query.NullCondition;
import hu.webarticum.minibase.query.query.NullsOrderMode;
import hu.webarticum.minibase.query.query.RangeCondition;
import hu.webarticum.minibase.query.query.SpecialCondition;
import hu.webarticum.minibase.query.query.VariableValue;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.RangeSelection;
import hu.webarticum.minibase.storage.api.Row;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.minibase.storage.api.TableSelection;
import hu.webarticum.minibase.storage.api.TableIndex.InclusionMode;
import hu.webarticum.minibase.storage.api.TableIndex.NullsMode;
import hu.webarticum.minibase.storage.api.TableIndex.SortMode;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator;
import hu.webarticum.minibase.storage.impl.simple.SimpleSelection;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator.MultiComparatorBuilder;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.util.FilteringIterator;
import hu.webarticum.miniconnect.util.GroupingIterator;
import hu.webarticum.miniconnect.util.LimitingIterator;
import hu.webarticum.miniconnect.util.SortedLimitingIterator;
import hu.webarticum.miniconnect.util.SortingIterator;

public class TableQueryUtil {
    
    // TODO: move this to StorageAccess or similar place
    private static final DefaultConverter CONVERTER = new DefaultConverter();
    

    private TableQueryUtil() {
        // utility class
    }


    public static void checkFields(Table table, Iterable<String> columnNames) {
        columnNames.forEach(n -> checkField(table, n));
    }

    public static void checkField(Table table, String columnName) {
        if (!table.columns().contains(columnName)) {
            throw PredefinedError.COLUMN_NOT_FOUND.toException(table.name(), columnName);
        }
    }
    
    public static Map<String, Object> mergeAndConvertFilters(
            ImmutableList<WhereItem> where, Table table, SessionState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        NamedResourceStore<Column> columns = table.columns();
        for (WhereItem whereItem : where) {
            String fieldName = whereItem.fieldName();
            Object newRawValue = whereItem.value();
            ColumnDefinition columnDefinition = columns.get(fieldName).definition();
            applyFilterValue(result, fieldName, newRawValue, columnDefinition, state);
        }
        return result;
    }

    public static void applyFilterValue(
            Map<String, Object> subFilter,
            String key,
            Object newRawValue,
            ColumnDefinition columnDefinition,
            SessionState state) {
        Object existingValue = subFilter.get(key);
        Object convertedValue = newRawValue;
        Class<?> clazz = columnDefinition.clazz();
        if (convertedValue instanceof VariableValue) {
            String variableName = ((VariableValue) convertedValue).name();
            convertedValue = state.getUserVariable(variableName);
            convertedValue = TableQueryUtil.convert(convertedValue, clazz);
        } else if (convertedValue instanceof RangeCondition) {
            RangeCondition rangeCondition = (RangeCondition) convertedValue;
            Object convertedFrom = TableQueryUtil.convert(rangeCondition.from(), clazz);
            Object convertedTo = TableQueryUtil.convert(rangeCondition.to(), clazz);
            convertedValue = new RangeCondition(
                    convertedFrom, rangeCondition.fromInclusive(), convertedTo, rangeCondition.toInclusive());
        } else if (!(convertedValue instanceof SpecialCondition)) {
            convertedValue = TableQueryUtil.convert(convertedValue, clazz);
        }
        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) columnDefinition.comparator();
        subFilter.put(key, mergeFilterValue(existingValue, convertedValue, comparator));
    }

    public static Object mergeFilterValue(Object existingValue, Object newValue, Comparator<Object> comparator) {
        if (existingValue == null) {
            return newValue;
        }
        
        if (
                !(existingValue instanceof SpecialCondition) &&
                !(newValue instanceof SpecialCondition) &&
                comparator.compare(newValue, existingValue) == 0) {
            return newValue;
        }
        
        if (existingValue == NullCondition.IS_NOT_NULL) {
            if (newValue == NullCondition.IS_NULL) {
                throw new IncompatibleFiltersException(existingValue, newValue);
            }
            return newValue;
        } else if (existingValue == NullCondition.IS_NULL) {
            if (newValue != NullCondition.IS_NULL) {
                throw new IncompatibleFiltersException(existingValue, newValue);
            }
            return newValue;
        } else if (existingValue instanceof RangeCondition) {
            if (!(newValue instanceof RangeCondition)) {
                throw new IncompatibleFiltersException(existingValue, newValue);
            }
            return mergeRangeConditions((RangeCondition) existingValue, (RangeCondition) newValue, comparator);
        } else {
            throw new IncompatibleFiltersException(existingValue, newValue);
        }
    }
    
    private static RangeCondition mergeRangeConditions(
            RangeCondition existingRange, RangeCondition newRange, Comparator<Object> comparator) {
        boolean narrowFrom = checkNarrowing(
                existingRange.from(),
                existingRange.fromInclusive(),
                newRange.from(),
                newRange.fromInclusive(),
                comparator);
        boolean narrowTo = checkNarrowing(
                existingRange.to(),
                existingRange.toInclusive(),
                newRange.to(),
                newRange.toInclusive(),
                comparator.reversed());
        
        Object from = narrowFrom ? newRange.from() : existingRange.from();
        boolean fromInclusive = narrowFrom ? newRange.fromInclusive() : existingRange.fromInclusive();
        Object to = narrowTo ? newRange.to() : existingRange.to();
        boolean toInclusive = narrowTo ? newRange.toInclusive() : existingRange.toInclusive();
        
        return new RangeCondition(from, fromInclusive, to, toInclusive);
    }
    
    private static boolean checkNarrowing(
            Object value, boolean inclusive, Object liftValue, boolean liftInclusive, Comparator<Object> comparator) {
        if (liftValue == null) {
            return false;
        } else if (value == null) {
            return true;
        }
        
        int cmp = comparator.compare(value, liftValue);
        if (cmp > 0) {
            return false;
        } else if (cmp < 0) {
            return true;
        }
        
        if (inclusive == liftInclusive) {
            return false;
        }
        
        return !liftInclusive;
    }

    public static LargeInteger countRows(Table table, Map<String, Object> queryWhere) {
        Map<ImmutableList<String>, TableIndex> indexesByColumnName = new LinkedHashMap<>();
        Set<String> unindexedColumnNames = collectIndexes(table, queryWhere, indexesByColumnName);
        
        List<TableSelection> moreSelections = new ArrayList<>();
        TableSelection firstSelection = collectIndexSelections(
                table.size(), queryWhere, Collections.emptyList(), indexesByColumnName, moreSelections);

        LargeInteger result = LargeInteger.ZERO;
        for (LargeInteger rowIndex : firstSelection) {
            if (isRowMatchingWithMore(table, rowIndex, queryWhere, moreSelections, unindexedColumnNames)) {
                result = result.add(LargeInteger.ONE);
            }
        }
        return result;
    }

    public static boolean isColumnContainingValue(Table table, String columnName, Object value) {
        Map<String, Object> filter = new HashMap<>();
        filter.put(columnName, value);
        Iterator<LargeInteger> iterator = filterRows(table, filter, Collections.emptyList(), null);
        return iterator.hasNext();
    }
    
    public static List<LargeInteger> filterRowsToList(
            Table table, Map<String, Object> filter, List<OrderByEntry> orderBy, LargeInteger limit) {
        Iterator<LargeInteger> iterator = filterRows(table, filter, orderBy, limit);
        return collectIterator(iterator);
    }
    
    public static Iterator<LargeInteger> filterRows(
            Table table, Map<String, Object> filter, List<OrderByEntry> orderBy, LargeInteger limit) {
        Set<String> filterIndexColumns = new HashSet<>(filter.keySet());
        List<OrderByEntry> matchedOrderByEntries = new ArrayList<>();
        List<String> matchedFilterColumns = new ArrayList<>();
        TableIndex orderIndex = findOrderIndex(
                table, orderBy, filterIndexColumns, matchedOrderByEntries, matchedFilterColumns);
        if (orderIndex != null) {
            filterIndexColumns.removeAll(matchedFilterColumns);
        }
        Map<ImmutableList<String>, TableIndex> indexesByColumnName = new LinkedHashMap<>();
        Set<String> unindexedColumnNames = collectIndexes(table, filter, indexesByColumnName);
        indexesByColumnName = prependIndex(matchedFilterColumns, orderIndex, indexesByColumnName);

        List<TableSelection> moreSelections = new ArrayList<>();
        TableSelection firstSelection = collectIndexSelections(
                table.size(), filter, matchedOrderByEntries, indexesByColumnName, moreSelections);
        
        Iterator<LargeInteger> result = matchRows(table, filter, firstSelection, moreSelections, unindexedColumnNames);
        
        if (!orderBy.isEmpty() && orderIndex == null) {
            MultiComparator rowComparator = createMultiComparator(orderBy, s -> table);
            Comparator<LargeInteger> rowIndexComparator = createRowIndexComparator(rowComparator, table, orderBy);
            if (limit != null) {
                result = new SortedLimitingIterator<>(result, rowIndexComparator, limit);
            } else {
                result = new SortingIterator<>(result, rowIndexComparator);
            }
        } else if (matchedOrderByEntries.size() < orderBy.size()) {
            MultiComparator outerRowComparator = createMultiComparator(matchedOrderByEntries, s -> table);
            Comparator<LargeInteger> outerRowIndexComparator = createRowIndexComparator(
                    outerRowComparator, table, matchedOrderByEntries);
            MultiComparator innerRowComparator = createMultiComparator(orderBy, s -> table);
            Comparator<LargeInteger> innerRowIndexComparator = createRowIndexComparator(
                    innerRowComparator, table, orderBy);
            result = new GroupingIterator<>(
                    result,
                    outerRowIndexComparator,
                    (Iterator<LargeInteger> groupItems, LargeInteger position) -> sortGroup(
                            groupItems, innerRowIndexComparator, position, limit)
            );
        } else if (limit != null) {
            result = new LimitingIterator<>(result, limit);
        }
        
        return result;
    }
    
    private static Iterator<LargeInteger> sortGroup(
            Iterator<LargeInteger> groupItems,
            Comparator<LargeInteger> comparator,
            LargeInteger position,
            LargeInteger limit) {
        if (limit == null) {
            return new SortingIterator<>(groupItems, comparator);
        }
        
        LargeInteger remainingLimit = limit.subtract(position);
        if (remainingLimit.isLessThanOrEqualTo(LargeInteger.ZERO)) {
            return null;
        }
        
        return new SortedLimitingIterator<>(groupItems, comparator, remainingLimit);
    }
    
    private static Comparator<LargeInteger> createRowIndexComparator(
            MultiComparator rowComparator, Table table, List<OrderByEntry> orderByEntries) {
        return (i1, i2) -> rowComparator.compare(
                TableQueryUtil.extractOrderValues(orderByEntries, s -> table, s -> i1),
                TableQueryUtil.extractOrderValues(orderByEntries, s -> table, s -> i2));
    }
    
    private static TableIndex findOrderIndex(
            Table table,
            List<OrderByEntry> orderBy,
            Set<String> filterIndexColumns,
            List<OrderByEntry> matchedOrderByEntries,
            List<String> matchedFilterColumns) {
        if (orderBy.isEmpty()) {
            return null;
        }
        
        Set<String> columnNames = filterIndexColumns;
        if (columnNames.isEmpty()) {
            columnNames = new HashSet<>(table.columns().names().asList());
        }
        
        List<OrderByEntry> indexableOrderByEntries = new ArrayList<>();
        for (OrderByEntry orderByEntry : orderBy) {
            if (!columnNames.contains(orderByEntry.fieldName)) {
                break;
            }
            indexableOrderByEntries.add(orderByEntry);
        }
        if (indexableOrderByEntries.isEmpty()) {
            return null;
        }

        List<OrderByEntry> maxOrderByEntries = new ArrayList<>();
        List<String> maxfilterColumns = new ArrayList<>();
        TableIndex result = null;
        for (TableIndex tableIndex : table.indexes().resources()) {
            List<OrderByEntry> orderByEntriesOut = new ArrayList<>();
            List<String> filterColumnsOut = new ArrayList<>();
            if (matchOrderByIndex(
                    tableIndex, indexableOrderByEntries, columnNames, orderByEntriesOut, filterColumnsOut)) {
                int matchLength = orderByEntriesOut.size();
                int filterCount = filterColumnsOut.size();
                int maxMatchLength = maxOrderByEntries.size();
                int maxMatchMaxFilterCount = maxfilterColumns.size();
                if (
                        matchLength > maxMatchLength ||
                        (matchLength == maxMatchLength && filterCount > maxMatchMaxFilterCount)) {
                    maxOrderByEntries = orderByEntriesOut;
                    maxfilterColumns = filterColumnsOut;
                    result = tableIndex;
                }
            }
        }
        matchedOrderByEntries.addAll(maxOrderByEntries);
        matchedFilterColumns.addAll(maxfilterColumns);
        return result;
    }
    
    private static boolean matchOrderByIndex(
            TableIndex tableIndex,
            List<OrderByEntry> orderByEntries,
            Set<String> columnNames,
            List<OrderByEntry> orderByEntriesOut,
            List<String> filterColumnsOut) {
        boolean result = false;
        ImmutableList<String> indexColumnNames = tableIndex.columnNames();
        
        int indexLength = indexColumnNames.size();
        int orderLength = Math.min(indexLength, orderByEntries.size());
        for (int i = 0; i < orderLength; i++) {
            String indexColumnName = indexColumnNames.get(i);
            OrderByEntry orderByEntry = orderByEntries.get(i);
            if (!orderByEntry.fieldName.equals(indexColumnName)) {
                return result;
            }
            result = true;
            orderByEntriesOut.add(orderByEntry);
            filterColumnsOut.add(orderByEntry.fieldName);
        }
        
        for (int i = orderLength; i < indexLength; i++) {
            String indexColumnName = indexColumnNames.get(i);
            if (!columnNames.contains(indexColumnName)) {
                break;
            }
            filterColumnsOut.add(indexColumnName);
        }
        
        return result;
    }
    
    private static Map<ImmutableList<String>, TableIndex> prependIndex(
            List<String> columnNames, TableIndex index, Map<ImmutableList<String>, TableIndex> indexes) {
        if (index == null) {
            return indexes;
        }
        
        Map<ImmutableList<String>, TableIndex> result = new LinkedHashMap<>();
        result.put(ImmutableList.fromCollection(columnNames), index);
        result.putAll(indexes);
        return result;
    }
    
    private static Set<String> collectIndexes(
            Table table, Map<String, Object> queryWhere, Map<ImmutableList<String>, TableIndex> map) {
        NamedResourceStore<TableIndex> indexStore = table.indexes();
        ImmutableList<TableIndex> indexes = indexStore.resources();
        int maxIndexColumnCount = calculateMaxIndexColumnCount(indexes);
        int maxMatchingColumnCount = Math.min(queryWhere.size(), maxIndexColumnCount);
        Set<String> result = new LinkedHashSet<>(queryWhere.keySet());
        for (int columnCount = maxMatchingColumnCount; columnCount > 0; columnCount--) {
            for (TableIndex tableIndex : indexes) {
                ImmutableList<String> indexColumnNames = tableIndex.columnNames();
                if (areColumnsMatching(indexColumnNames, result, columnCount, queryWhere)) {
                    ImmutableList<String> matchedColumnNames =
                            indexColumnNames.section(0, columnCount);
                    map.put(matchedColumnNames, tableIndex);
                    result.removeAll(matchedColumnNames.asList());
                }
            }
        }
        return result;
    }

    private static int calculateMaxIndexColumnCount(ImmutableList<TableIndex> indexes) {
        int maxIndexColumnCount = 0;
        for (TableIndex tableIndex : indexes) {
            int indexColumnCount = tableIndex.columnNames().size();
            if (indexColumnCount > maxIndexColumnCount) {
                maxIndexColumnCount = indexColumnCount;
            }
        }
        return maxIndexColumnCount;
    }

    private static boolean areColumnsMatching(
            ImmutableList<String> indexColumnNames,
            Set<String> availableColumnNames,
            int columnCount,
            Map<String, Object> queryWhere) {
        if (indexColumnNames.size() < columnCount) {
            return false;
        }
        
        for (int i = 0; i < columnCount; i++) {
            String columnName = indexColumnNames.get(i);
            if (!availableColumnNames.contains(columnName)) {
                return false;
            } else if (i < columnCount - 1) {
                Object value = queryWhere.get(columnName);
                if (value instanceof RangeCondition) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private static TableSelection collectIndexSelections(
            LargeInteger tableSize,
            Map<String, Object> filter,
            List<OrderByEntry> orderBy,
            Map<ImmutableList<String>, TableIndex> indexesByColumnName,
            List<TableSelection> moreSelections) {
        if (filter.containsValue(null)) {
            return new SimpleSelection(ImmutableList.empty());
        }
        
        TableSelection firstSelection = null;
        for (Map.Entry<ImmutableList<String>, TableIndex> entry : indexesByColumnName.entrySet()) {
            ImmutableList<String> columnNames = entry.getKey();
            TableIndex tableIndex = entry.getValue();
            ImmutableList<Object> values = columnNames.map(filter::get);
            ImmutableList<SortMode> sortModes;
            if (firstSelection == null && !orderBy.isEmpty()) {
                sortModes = values.map((i, v) -> getIndexNthSortMode(i, orderBy));
            } else {
                sortModes = values.map(v -> SortMode.UNSORTED);
            }
            TableSelection selection = tableIndex.findMulti(
                    values.map(TableQueryUtil::rangeFromForValue),
                    rangeFromInclusionModeForValues(values),
                    values.map(TableQueryUtil::rangeToForValue),
                    rangeToInclusionModeForValues(values),
                    values.map(TableQueryUtil::nullsModeForValue),
                    sortModes);
            if (firstSelection == null) {
                firstSelection = selection;
            } else {
                moreSelections.add(selection);
            }
        }
        if (firstSelection == null) {
            firstSelection = new RangeSelection(LargeInteger.ZERO, tableSize);
        }
        
        return firstSelection;
    }
    
    private static Object rangeFromForValue(Object value) {
        if (value instanceof RangeCondition) {
            return ((RangeCondition) value).from();
        } else if (value instanceof SpecialCondition) {
            return null;
        } else {
            return value;
        }
    }

    private static Object rangeToForValue(Object value) {
        if (value instanceof RangeCondition) {
            return ((RangeCondition) value).to();
        } else if (value instanceof SpecialCondition) {
            return null;
        } else {
            return value;
        }
    }

    private static InclusionMode rangeFromInclusionModeForValues(ImmutableList<Object> values) {
        RangeCondition rangeLastValue = extractRangeLastValue(values);
        if (rangeLastValue == null) {
            return InclusionMode.INCLUDE;
        }
        
        return rangeLastValue.fromInclusive() ? InclusionMode.INCLUDE : InclusionMode.EXCLUDE;
    }

    private static InclusionMode rangeToInclusionModeForValues(ImmutableList<Object> values) {
        RangeCondition rangeLastValue = extractRangeLastValue(values);
        if (rangeLastValue == null) {
            return InclusionMode.INCLUDE;
        }
        
        return rangeLastValue.toInclusive() ? InclusionMode.INCLUDE : InclusionMode.EXCLUDE;
    }

    private static RangeCondition extractRangeLastValue(ImmutableList<Object> values) {
        if (values.isEmpty()) {
            return null;
        }
        
        Object lastValue = values.get(values.size() - 1);
        if (!(lastValue instanceof RangeCondition)) {
            return null;
        }
        
        return (RangeCondition) lastValue;
    }

    private static NullsMode nullsModeForValue(Object value) {
        if (value == NullCondition.IS_NULL) {
            return NullsMode.NULLS_ONLY;
        } else if (value == NullCondition.IS_NOT_NULL) {
            return NullsMode.NO_NULLS;
        } else if (value instanceof RangeCondition) {
            return NullsMode.NO_NULLS;
        } else {
            return NullsMode.WITH_NULLS;
        }
    }
    
    private static SortMode getIndexNthSortMode(int i, List<OrderByEntry> orderBy) {
        if (orderBy.size() <= i) {
            return SortMode.UNSORTED;
        }
        
        return getSortModeOf(orderBy.get(i));
    }
    
    private static SortMode getSortModeOf(OrderByEntry orderByEntry) {
        boolean nullsFirst;
        if (orderByEntry.nullsOrderMode == NullsOrderMode.NULLS_AUTO) {
            nullsFirst = orderByEntry.ascOrder;
        } else {
            nullsFirst = (orderByEntry.nullsOrderMode == NullsOrderMode.NULLS_FIRST);
        }
        
        if (orderByEntry.ascOrder) {
            return nullsFirst ? SortMode.ASC_NULLS_FIRST : SortMode.ASC_NULLS_LAST;
        } else {
            return nullsFirst ? SortMode.DESC_NULLS_FIRST : SortMode.DESC_NULLS_LAST;
        }
    }
    
    private static Iterator<LargeInteger> matchRows(
            Table table,
            Map<String, Object> queryWhere,
            TableSelection firstSelection,
            List<TableSelection> moreSelections,
            Set<String> unindexedColumnNames) {
        if (moreSelections.isEmpty() && unindexedColumnNames.isEmpty()) {
            return firstSelection.iterator();
        }
        
        return new FilteringIterator<>(
                firstSelection.iterator(),
                rowIndex -> isRowMatchingWithMore(table, rowIndex, queryWhere, moreSelections, unindexedColumnNames));
    }

    private static boolean isRowMatchingWithMore(
            Table table,
            LargeInteger rowIndex,
            Map<String, Object> queryWhere,
            List<TableSelection> moreSelections,
            Set<String> unindexedColumnNames) {
        for (TableSelection selection : moreSelections) {
            if (!selection.containsRow(rowIndex)) {
                return false;
            }
        }
        
        if (!unindexedColumnNames.isEmpty()) {
            for (String columnName : unindexedColumnNames) {
                Column column = table.columns().get(columnName);
                Object expectedValue = queryWhere.get(columnName);
                Object actualValue = table.row(rowIndex).get(columnName);
                if (!isValueMatching(expectedValue, actualValue, column)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isValueMatching(Object expectedValue, Object actualValue, Column column) {
        if (expectedValue == NullCondition.IS_NULL) {
            return actualValue == null;
        } else if (expectedValue == NullCondition.IS_NOT_NULL) {
            return actualValue != null;
        } else if (actualValue == null) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) column.definition().comparator();
        if (expectedValue instanceof RangeCondition) {
            return checkRange((RangeCondition) expectedValue, actualValue, comparator);
        }
        
        return comparator.compare(actualValue, expectedValue) == 0;
    }

    private static boolean checkRange(
            RangeCondition rangeCondition, Object actualValue, Comparator<Object> comparator) {
        Object from = rangeCondition.from();
        if (from != null) {
            boolean fromInclusive = rangeCondition.fromInclusive();
            int fromCmp = comparator.compare(actualValue, from);
            if ((fromInclusive && fromCmp < 0) || (!fromInclusive && fromCmp <= 0)) {
                return false;
            }
        }

        Object to = rangeCondition.to();
        if (to != null) {
            boolean toInclusive = rangeCondition.toInclusive();
            int toCmp = comparator.compare(actualValue, to);
            if ((toInclusive && toCmp > 0) || (!toInclusive && toCmp >= 0)) {
                return false;
            }
        }
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object source, Class<T> targetClazz) {
        return (T) CONVERTER.convert(source, targetClazz);
    }

    public static Map<String, Object> convertColumnNewValues(
            Table table, Map<String, Object> columnValues, SessionState state, boolean check) {
        Map<String, Object> result = new LinkedHashMap<>();
        NamedResourceStore<Column> columns = table.columns();
        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();
            Object convertedValue = value;
            ColumnDefinition definition = columns.get(columnName).definition();
            Class<?> columnClazz = definition.clazz();
            if (convertedValue instanceof VariableValue) {
                String variableName = ((VariableValue) value).name();
                convertedValue = state.getUserVariable(variableName);
                convertedValue = convert(convertedValue, columnClazz);
            }
            if (!(convertedValue instanceof SpecialCondition)) {
                convertedValue = convert(convertedValue, columnClazz);
            }
            result.put(columnName, convertedValue);
        }
        return result;
    }

    public static ImmutableMap<Integer, Object> toByColumnPoisitionedImmutableMap(
            Table table, Map<String, Object> columnValues) {
        NamedResourceStore<Column> columns = table.columns();
        ImmutableList<String> columnNames = columns.names();
        return ImmutableMap.fromMap(columnValues).map(columnNames::indexOf, v -> v);
    }

    public static Column getAutoIncrementedColumn(Table table) {
        for (Column column : table.columns().resources()) {
            if (column.definition().isAutoIncremented()) {
                return column;
            }
        }
        
        return null;
    }
    
    public static List<LargeInteger> findAllNonNull(Table table, String columnName, Object value) {
        List<LargeInteger> result = new ArrayList<>();
        for (TableIndex tableIndex : table.indexes().resources()) {
            if (tableIndex.columnNames().get(0).equals(columnName)) {
                tableIndex.find(value).forEach(result::add);
                return result;
            }
        }
        
        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) table.columns().get(columnName).definition().comparator();
        LargeInteger size = table.size();
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(LargeInteger.ONE)) {
            Row row = table.row(i);
            Object foundValue = row.get(columnName);
            if (foundValue != null && comparator.compare(value, foundValue) == 0) {
                result.add(i);
            }
        }
        
        return result;
    }

    private static <T> List<T> collectIterator(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
    
    public static MultiComparator createMultiComparator(
            List<OrderByEntry> orderByEntries, Function<String, Table> tableResolver) {
        MultiComparatorBuilder builder = MultiComparator.builder();
        for (OrderByEntry orderByEntry : orderByEntries) {
            String columnName = orderByEntry.fieldName;
            Table table = tableResolver.apply(orderByEntry.tableAlias);
            ColumnDefinition columnDefinition = table.columns().get(columnName).definition();
            Comparator<?> columnComparator = columnDefinition.comparator();
            boolean nullsLow = isNullsLow(orderByEntry);
            builder.add(columnComparator, true, orderByEntry.ascOrder, nullsLow);
        }
        return builder.build();
    }
    
    public static boolean isNullsLow(OrderByEntry orderByEntry) {
        if (orderByEntry.nullsOrderMode == NullsOrderMode.NULLS_AUTO) {
            return true;
        } else {
            boolean nullsFirst = (orderByEntry.nullsOrderMode == NullsOrderMode.NULLS_FIRST);
            return (orderByEntry.ascOrder == nullsFirst);
        }
    }

    public static ImmutableList<Object> extractOrderValues(
            List<OrderByEntry> orderByEntries,
            Function<String, Table> tableResolver,
            Function<String, LargeInteger> rowIndexResolver) {
        List<Object> result = new ArrayList<>(orderByEntries.size());
        Map<String, Row> rowCache = new HashMap<>();
        for (OrderByEntry orderByEntry : orderByEntries) {
            LargeInteger rowIndex = rowIndexResolver.apply(orderByEntry.tableAlias);
            if (rowIndex != null) {
                Table table = tableResolver.apply(orderByEntry.tableAlias);
                Row row = rowCache.computeIfAbsent(orderByEntry.tableAlias, a -> table.row(rowIndex));
                result.add(row.get(orderByEntry.fieldName));
            } else {
                result.add(null);
            }
        }
        return ImmutableList.fromCollection(result);
    }
    
    public static Object getSpecialValue(SpecialValueParameter specialValueParameter, SessionState state) {
        switch (specialValueParameter) {
            case CURRENT_USER:
                return "";
            case CURRENT_SCHEMA:
                return state.getCurrentSchema();
            case CURRENT_CATALOG:
                return state.getCurrentSchema();
            case READONLY:
                return false;
            case AUTOCOMMIT:
                return true;
            case LAST_INSERT_ID:
                return state.getLastInsertId();
            default:
                throw PredefinedError.OTHER_ERROR.toException();
        }
    }
    
}
