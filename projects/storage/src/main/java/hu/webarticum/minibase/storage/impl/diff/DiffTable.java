package hu.webarticum.minibase.storage.impl.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.storage.api.AbstractNamedResourceStoreDecorator;
import hu.webarticum.minibase.storage.api.AbstractTableDecorator;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Row;
import hu.webarticum.minibase.storage.api.Sequence;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.minibase.storage.api.TableIndex.InclusionMode;
import hu.webarticum.minibase.storage.api.TableIndex.NullsMode;
import hu.webarticum.minibase.storage.api.TableIndex.SortMode;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.api.TableSelection;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumn;
import hu.webarticum.minibase.storage.impl.simple.SimpleRow;
import hu.webarticum.minibase.storage.impl.simple.SimpleSequence;
import hu.webarticum.minibase.storage.util.ComparatorUtil;
import hu.webarticum.minibase.storage.util.SelectionPredicate;
import hu.webarticum.minibase.storage.util.TablePatchUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.util.ChainedIterator;
import hu.webarticum.miniconnect.util.FilteringIterator;
import hu.webarticum.miniconnect.util.IteratorAdapter;

public class DiffTable extends AbstractTableDecorator {

    private final DiffTableColumnStore columnStore;

    private final DiffTableIndexStore indexStore;

    private final List<ImmutableList<Object>> insertedRows = new ArrayList<>();

    private final NavigableMap<LargeInteger, ImmutableMap<Integer, Object>> updates = new TreeMap<>();

    private final NavigableSet<LargeInteger> deletions = new TreeSet<>();

    private final SimpleSequence sequence;

    private volatile boolean hasChanges = false;

    private volatile long changeVersion = 0L;


    public DiffTable(Table baseTable) {
        super(baseTable);
        this.columnStore = new DiffTableColumnStore();
        this.indexStore = new DiffTableIndexStore();
        this.sequence = new SimpleSequence(baseTable.sequence().get());
    }


    @Override
    public NamedResourceStore<Column> columns() {
        return columnStore;
    }

    @Override
    public NamedResourceStore<TableIndex> indexes() {
        return indexStore;
    }

    @Override
    public LargeInteger size() {
        return baseTable.size()
                .add(LargeInteger.of(insertedRows.size()))
                .subtract(LargeInteger.of(deletions.size()));
    }

    @Override
    public Row row(LargeInteger rowIndex) {
        if (!hasChanges) {
            return baseTable.row(rowIndex);
        }

        return rowInternal(rowIndex);
    }

    private synchronized Row rowInternal(LargeInteger rowIndex) {
        LargeInteger baseTableSize = baseTable.size();
        LargeInteger adjustedRowIndex = adjustByDeletions(LargeInteger.ZERO, rowIndex);

        if (adjustedRowIndex.isGreaterThanOrEqualTo(baseTableSize)) {
            ImmutableList<Object> rowData =
                    insertedRows.get(adjustedRowIndex.subtract(baseTableSize).intValueExact());
            return new SimpleRow(baseTable.columns().names(), rowData);
        }

        Row originalRow = baseTable.row(adjustedRowIndex);
        ImmutableMap<Integer, Object> rowUpdates = updates.get(adjustedRowIndex);
        if (rowUpdates == null) {
            return originalRow;
        }

        return new UpdatedRow(originalRow, rowUpdates);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public synchronized void applyPatch(TablePatch patch) {
        if (
                patch.insertedRows().isEmpty() &&
                patch.updates().isEmpty() &&
                patch.deletions().isEmpty()) {
            return;
        }

        TablePatchUtil.checkIndividualValues(this, patch);
        checkUniqueInPatch(patch);

        insertedRows.addAll(patch.insertedRows());
        applyUpdates(patch.updates());
        applyDeletions(patch.deletions());

        changeVersion++;
        hasChanges = !insertedRows.isEmpty() || !updates.isEmpty() || !deletions.isEmpty();
    }

    private void checkUniqueInPatch(TablePatch patch) {
        ImmutableList<ColumnDefinition> columnDefinitions = columnStore.resources().map(Column::definition);
        Map<Integer, Set<Object>> uniqueColumnValues = new HashMap<>();
        int columnCount = columnDefinitions.size();
        for (int i = 0; i < columnCount; i++) {
            ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (columnDefinition.isUnique()) {
                @SuppressWarnings("unchecked")
                Comparator<Object> comparator = (Comparator<Object>) columnDefinition.comparator();
                uniqueColumnValues.put(i, new TreeSet<>(comparator));
            }
        }
        if (uniqueColumnValues.isEmpty()) {
            return;
        }

        for (ImmutableMap<Integer, Object> rowUpdates : patch.updates().values()) {
            for (Map.Entry<Integer, Object> updateEntry : rowUpdates.entrySet()) {
                checkAndAddUniqueValue(updateEntry.getKey(), updateEntry.getValue(), uniqueColumnValues);
            }
        }
        for (ImmutableList<Object> insertedRow : patch.insertedRows()) {
            for (Integer columnIndex : uniqueColumnValues.keySet()) {
                checkAndAddUniqueValue(columnIndex, insertedRow.get(columnIndex), uniqueColumnValues);
            }
        }

        for (Map.Entry<Integer, Set<Object>> entry : uniqueColumnValues.entrySet()) {
            checkUniqueColumnPatch(entry.getKey(), entry.getValue(), patch);
        }
    }

    private void checkUniqueColumnPatch(int columnIndex, Set<Object> newValues, TablePatch patch) {
        if (newValues.isEmpty()) {
            return;
        }

        String columnName = columnStore.resources().get(columnIndex).name();
        ImmutableList<String> columnNameList = ImmutableList.of(columnName);
        TableIndex index = null;
        for (TableIndex potentialIndex : indexStore.resources()) {
            if (potentialIndex.columnNames().equals(columnNameList)) {
                index = potentialIndex;
                break;
            }
        }
        if (index != null) {
            checkUniqueColumnPatchWithIndex(columnIndex, newValues, patch, index);
        } else {
            checkUniqueColumnPatchWithFullTableScan(columnIndex, newValues, patch);
        }
    }

    private void checkUniqueColumnPatchWithIndex(
            Integer columnIndex, Set<Object> newValues, TablePatch patch, TableIndex index) {
        String columnName = columnStore.resources().get(columnIndex).name();
        for (Object newValue : newValues) {
            TableSelection selection = index.find(newValue);
            for (LargeInteger rowIndex : selection) {
                if (!isFieldUpdatedIn(rowIndex, columnIndex, patch)) {
                    throw PredefinedError.COLUMN_VALUE_NOT_UNIQUE.toException(columnName, newValue);
                }
            }
        }
    }

    private void checkUniqueColumnPatchWithFullTableScan(Integer columnIndex, Set<Object> newValues, TablePatch patch) {
        String columnName = columnStore.resources().get(columnIndex).name();
        LargeInteger tableSize = size();
        for (
                LargeInteger rowIndex = LargeInteger.ZERO;
                rowIndex.isLessThan(tableSize);
                rowIndex = rowIndex.increment()) {
            if (!isFieldUpdatedIn(rowIndex, columnIndex, patch)) {
                Object value = row(rowIndex).get(columnIndex);
                if (newValues.contains(value)) {
                    throw PredefinedError.COLUMN_VALUE_NOT_UNIQUE.toException(columnName, value);
                }
            }
        }
    }

    private boolean isFieldUpdatedIn(LargeInteger rowIndex, Integer columnIndex, TablePatch patch) {
        if (patch.deletions().contains(rowIndex)) {
            return true;
        }

        ImmutableMap<Integer, Object> updatedRow = patch.updates().get(rowIndex);
        return updatedRow != null && updatedRow.containsKey(columnIndex);
    }

    private void checkAndAddUniqueValue(
            int columnIndex, Object newValue, Map<Integer, Set<Object>> uniqueColumnValues) {
        if (newValue == null) {
            return;
        }

        Set<Object> values = uniqueColumnValues.get(columnIndex);
        if (values != null && !values.add(newValue)) {
            String columnName = columnStore.resources().get(columnIndex).name();
            throw PredefinedError.COLUMN_VALUE_NOT_UNIQUE.toException(columnName, newValue);
        }
    }

    private void applyUpdates(
            NavigableMap<LargeInteger, ImmutableMap<Integer, Object>> patchUpdates) {
        LargeInteger baseTableSize = baseTable.size();
        LargeInteger internalPosition = LargeInteger.ZERO;
        LargeInteger viewPosition = LargeInteger.ZERO;
        for (Map.Entry<LargeInteger, ImmutableMap<Integer, Object>> entry :
                patchUpdates.entrySet()) {
            LargeInteger rowIndex = entry.getKey();
            ImmutableMap<Integer, Object> rowUpdates = entry.getValue();
            LargeInteger remainingCount = rowIndex.subtract(viewPosition);
            LargeInteger adjustedRowIndex = adjustByDeletions(internalPosition, remainingCount);

            applyUpdate(adjustedRowIndex, rowUpdates, baseTableSize);

            internalPosition = adjustedRowIndex.add(LargeInteger.ONE);
            viewPosition = rowIndex.add(LargeInteger.ONE);
        }
    }

    private void applyUpdate(
            LargeInteger adjustedRowIndex,
            ImmutableMap<Integer, Object> rowUpdates,
            LargeInteger baseTableSize) {
        if (adjustedRowIndex.isLessThan(baseTableSize)) {
            ImmutableMap<Integer, Object> currentRowUpdates = updates.get(adjustedRowIndex);
            ImmutableMap<Integer, Object> newRowUpdates;
            if (currentRowUpdates == null) {
                newRowUpdates = rowUpdates;
            } else {
                newRowUpdates = currentRowUpdates.merge(rowUpdates);
            }
            updates.put(adjustedRowIndex, newRowUpdates);
        } else {
            int insertIndex = adjustedRowIndex.subtract(baseTableSize).intValueExact();
            ImmutableList<Object> currentRow = insertedRows.get(insertIndex);
            ImmutableList<Object> updatedRow = currentRow.map(rowUpdates::getOrDefault);
            insertedRows.set(insertIndex, updatedRow);
        }
    }

    private void applyDeletions(NavigableSet<LargeInteger> patchDeletions) {
        LargeInteger baseTableSize = baseTable.size();
        LargeInteger currentDeletionCount = LargeInteger.of(deletions.size());
        LargeInteger reducedSize = baseTableSize.subtract(currentDeletionCount);
        applyInnerDeletions(patchDeletions.headSet(reducedSize));
        applyOuterDeletions(patchDeletions.tailSet(reducedSize, true), reducedSize);
    }

    private void applyInnerDeletions(SortedSet<LargeInteger> innerDeletions) {
        LargeInteger internalPosition = LargeInteger.ZERO;
        LargeInteger viewPosition = LargeInteger.ZERO;
        for (LargeInteger rowIndex : innerDeletions) {
            LargeInteger remainingCount = rowIndex.subtract(viewPosition);
            LargeInteger adjustedRowIndex = adjustByDeletions(internalPosition, remainingCount);

            deletions.add(adjustedRowIndex);
            updates.remove(adjustedRowIndex);

            internalPosition = adjustedRowIndex.add(LargeInteger.ONE);
            viewPosition = rowIndex.add(LargeInteger.ONE);
        }
    }

    private void applyOuterDeletions(
            NavigableSet<LargeInteger> outerDeletions, LargeInteger reducedSize) {
        Iterator<LargeInteger> descIterator = outerDeletions.descendingIterator();
        while (descIterator.hasNext()) {
            LargeInteger outerIndex = descIterator.next();
            int insertionIndex = outerIndex.subtract(reducedSize).intValueExact();
            insertedRows.remove(insertionIndex);
        }
    }

    private LargeInteger adjustByDeletions(LargeInteger start, LargeInteger count) {
        return DiffTableUtil.adjustByDeletions(deletions, start, count);
    }

    @Override
    public Sequence sequence() {
        return sequence;
    }


    private static class UpdatedRow implements Row {

        private final Row baseRow;

        private final ImmutableMap<Integer, Object> updates;


        private UpdatedRow(Row baseRow, ImmutableMap<Integer, Object> updates) {
            this.baseRow = baseRow;
            this.updates = updates;
        }


        @Override
        public ImmutableList<String> columnNames() {
            return baseRow.columnNames();
        }

        @Override
        public Object get(int columnPosition) {
            if (updates.containsKey(columnPosition)) {
                return updates.get(columnPosition);
            } else {
                return baseRow.get(columnPosition);
            }
        }

        @Override
        public Object get(String columnName) {
            int columnPosition = baseRow.columnNames().indexOf(columnName);
            return get(columnPosition);
        }

        @Override
        public ImmutableList<Object> getAll() {
            List<Object> resultBuilder = new ArrayList<>();
            int width = baseRow.columnNames().size();
            for (int i = 0; i < width; i++) {
                resultBuilder.add(get(i));
            }
            return ImmutableList.fromCollection(resultBuilder);
        }

        @Override
        public ImmutableMap<String, Object> getMap() {
            return getMap(baseRow.columnNames());
        }

        @Override
        public ImmutableMap<String, Object> getMap(ImmutableList<String> columnNames) {
            Map<String, Object> resultBuilder = new HashMap<>();
            for (String columnName : columnNames) {
                resultBuilder.put(columnName, get(columnName));
            }
            return ImmutableMap.fromMap(resultBuilder);
        }

    }


    private class DiffTableColumnStore extends AbstractNamedResourceStoreDecorator<Column> {

        private DiffTableColumnStore() {
            super(baseTable.columns());
        }


        @Override
        public Column get(String name) {
            Column baseColumn = baseStore.get(name);
            if (!hasChanges) {
                return baseColumn;
            }

            Optional<ImmutableList<Object>> possibleValuesHolder = baseColumn.possibleValues();
            if (!possibleValuesHolder.isPresent()) {
                return baseColumn;
            } else {
                int columnPos = baseStore.names().indexOf(name);
                List<Object> updatedValues = new ArrayList<>(Math.min(1000, updates.size() + insertedRows.size()));
                for (ImmutableMap<Integer, Object> updateRow : updates.values()) {
                    Object value = updateRow.get(columnPos);
                    if (value != null) {
                        updatedValues.add(value);
                    }
                }
                for (ImmutableList<Object> insertedRow : insertedRows) {
                    Object value = insertedRow.get(columnPos);
                    if (value != null) {
                        updatedValues.add(value);
                    }
                }
                ColumnDefinition columnDefinition = baseColumn.definition();
                @SuppressWarnings("unchecked")
                Comparator<Object> comparator = (Comparator<Object>) columnDefinition.comparator();
                ImmutableList<Object> existingPossibleValues= possibleValuesHolder.get();
                ImmutableList<Object> mergedPossibleValues = DiffTableUtil.mergeUnique(
                        existingPossibleValues, updatedValues, comparator);
                return new SimpleColumn(name, columnDefinition, mergedPossibleValues);
            }
        }

    }


    private class DiffTableIndexStore extends AbstractNamedResourceStoreDecorator<TableIndex> {

        private final Map<String, TableIndex> cache = Collections.synchronizedMap(new HashMap<>());


        private DiffTableIndexStore() {
            super(baseTable.indexes());
        }


        @Override
        public TableIndex get(String name) {
            TableIndex baseIndex = baseStore.get(name);
            return baseIndex != null ? cache.computeIfAbsent(name, k -> new DiffTableIndex(baseIndex)) : null;
        }

    }


    private class DiffTableIndex implements TableIndex {

        private final TableIndex baseIndex;

        private final ImmutableList<Integer> columnIndexes;

        private long cachedChangeVersion = -1L;

        private DiffTableIndexState cachedState = null;


        private DiffTableIndex(TableIndex baseIndex) {
            this.baseIndex = baseIndex;
            ImmutableList<String> tableColumnNames = baseTable.columns().names();
            this.columnIndexes = baseIndex.columnNames().map(tableColumnNames::indexOf);
        }

        private DiffTableIndexState currentState() {
            long currentChangeVersion = changeVersion;
            if (cachedState == null || cachedChangeVersion != currentChangeVersion) {
                cachedState = createState();
                cachedChangeVersion = currentChangeVersion;
            }
            return cachedState;
        }

        private DiffTableIndexState createState() {
            int fullUpdateCount = updates.size() + insertedRows.size();
            NavigableSet<LargeInteger> stateDeletions = new TreeSet<>(deletions);
            Set<LargeInteger> stateUpdatedRowIndexes = new HashSet<>(fullUpdateCount);
            ArrayList<DiffTableIndexEntry> stateIndexEntries = new ArrayList<>(fullUpdateCount);

            LargeInteger position = LargeInteger.ZERO;
            LargeInteger fullDeletionCount = LargeInteger.ZERO;
            for (Map.Entry<LargeInteger, ImmutableMap<Integer, Object>> entry : updates.entrySet()) {
                LargeInteger baseRowIndex = entry.getKey();

                Collection<LargeInteger> subDeletions = stateDeletions.subSet(position, baseRowIndex);
                LargeInteger subDeletionCount = LargeInteger.of(subDeletions.size());
                fullDeletionCount = fullDeletionCount.add(subDeletionCount);
                LargeInteger rowIndex = baseRowIndex.subtract(fullDeletionCount);

                ImmutableMap<Integer, Object> rowUpdates = entry.getValue();
                boolean updated = false;
                for (Integer columnIndex : columnIndexes) {
                    if (rowUpdates.containsKey(columnIndex)) {
                        updated = true;
                        break;
                    }
                }

                if (updated) {
                    stateUpdatedRowIndexes.add(rowIndex);
                    Row baseRow = baseTable.row(baseRowIndex);
                    Row updatedRow = new UpdatedRow(baseRow, rowUpdates);
                    ImmutableList<Object> updatedData = columnIndexes.map(updatedRow::get);
                    DiffTableIndexEntry indexEntry = new DiffTableIndexEntry(rowIndex, updatedData);
                    stateIndexEntries.add(indexEntry);
                }

                position = baseRowIndex.add(LargeInteger.ONE);
            }
            Collection<LargeInteger> tailDeletions = stateDeletions.tailSet(position);
            LargeInteger tailDeletionCount = LargeInteger.of(tailDeletions.size());
            fullDeletionCount = fullDeletionCount.add(tailDeletionCount);
            LargeInteger innerSize = baseTable.size().subtract(fullDeletionCount);

            int insertionCount = insertedRows.size();
            for (int i = 0; i < insertionCount; i++) {
                ImmutableList<Object> insertedRow = insertedRows.get(i);
                ImmutableList<Object> insertedData = columnIndexes.map(insertedRow::get);
                LargeInteger rowIndex = LargeInteger.of(i).add(innerSize);
                DiffTableIndexEntry indexEntry = new DiffTableIndexEntry(rowIndex, insertedData);
                stateUpdatedRowIndexes.add(rowIndex);
                stateIndexEntries.add(indexEntry);
            }

            stateIndexEntries.trimToSize();
            return new DiffTableIndexState(stateDeletions, stateUpdatedRowIndexes, stateIndexEntries);
        }


        @Override
        public String name() {
            return baseIndex.name();
        }

        @Override
        public boolean isUnique() {
            return baseIndex.isUnique();
        }

        @Override
        public ImmutableList<String> columnNames() {
            return baseIndex.columnNames();
        }

        @Override
        public TableSelection findMulti(
                ImmutableList<?> from,
                InclusionMode fromInclusionMode,
                ImmutableList<?> to,
                InclusionMode toInclusionMode,
                ImmutableList<NullsMode> nullsModes,
                ImmutableList<SortMode> sortModes) {
            TableSelection baseSelection = baseIndex.findMulti(
                    from, fromInclusionMode, to, toInclusionMode, nullsModes, sortModes);
            if (!hasChanges) {
                return baseSelection;
            }

            DiffTableIndexState state;
            synchronized (DiffTable.this) {
                if (!hasChanges) {
                    return baseSelection;
                }
                state = currentState();
            }

            MultiComparator multiComparator = ComparatorUtil.createMultiComparator(
                    baseTable, baseIndex.columnNames(), sortModes);
            Predicate<ImmutableList<Object>> predicate = new SelectionPredicate(
                    from, fromInclusionMode, to, toInclusionMode, nullsModes, multiComparator);
            if (!sortModes.isEmpty() && sortModes.get(0).isSorted()) {
                return new SortedDiffTableSelection(
                        state,
                        baseIndex,
                        baseSelection,
                        predicate,
                        multiComparator,
                        from,
                        fromInclusionMode,
                        to,
                        toInclusionMode,
                        nullsModes,
                        sortModes);
            } else {
                return new UnsortedDiffTableSelection(state, baseSelection, predicate);
            }
        }

    }


    private static abstract class AbstractDiffTableSelection implements TableSelection {

        protected final DiffTableIndexState state;

        protected final TableSelection baseSelection;

        protected final Set<LargeInteger> filteredUpdatedRowIndexes;

        protected final ArrayList<DiffTableIndexEntry> filteredIndexEntries;


        protected AbstractDiffTableSelection(
                DiffTableIndexState state,
                TableSelection baseSelection,
                Predicate<ImmutableList<Object>> predicate) {
            this.state = state;
            this.baseSelection = baseSelection;
            this.filteredUpdatedRowIndexes = new HashSet<>();
            this.filteredIndexEntries = new ArrayList<>(state.indexEntries.size());
            for (DiffTableIndexEntry indexEntry : state.indexEntries) {
                if (predicate.test(indexEntry.values)) {
                    this.filteredUpdatedRowIndexes.add(indexEntry.rowIndex);
                    this.filteredIndexEntries.add(indexEntry);
                }
            }
            this.filteredIndexEntries.trimToSize();
        }


        @Override
        public boolean containsRow(LargeInteger rowIndex) {
            if (state.updatedRowIndexes.contains(rowIndex)) {
                return filteredUpdatedRowIndexes.contains(rowIndex);
            } else {
                LargeInteger adjustedRowIndex = DiffTableUtil.adjustByDeletions(
                        state.deletions, LargeInteger.ZERO, rowIndex);
                return baseSelection.containsRow(adjustedRowIndex);
            }
        }

        protected Iterator<LargeInteger> wrapIterator(Iterator<LargeInteger> baseIterator) {
            return new FilteringIterator<>(
                    new IteratorAdapter<>(
                            new FilteringIterator<>(
                                    baseIterator,
                                    v -> !state.deletions.contains(v)),
                            v -> DiffTableUtil.deadjustByDeletions(state.deletions, v)),
                    v -> !state.updatedRowIndexes.contains(v));
        }

    }


    private static class SortedDiffTableSelection extends AbstractDiffTableSelection {

        private final TableIndex baseIndex;

        private final ImmutableList<?> from;

        private final InclusionMode fromInclusionMode;

        private final ImmutableList<?> to;

        private final InclusionMode toInclusionMode;

        private final ImmutableList<NullsMode> nullsModes;

        private final ImmutableList<SortMode> sortModes;


        public SortedDiffTableSelection( // NOSONAR currently these many parameters are OK
                DiffTableIndexState state,
                TableIndex baseIndex,
                TableSelection baseSelection,
                Predicate<ImmutableList<Object>> predicate,
                MultiComparator multiComparator,
                ImmutableList<?> from,
                InclusionMode fromInclusionMode,
                ImmutableList<?> to,
                InclusionMode toInclusionMode,
                ImmutableList<NullsMode> nullsModes,
                ImmutableList<SortMode> sortModes) {
            super(state, baseSelection, predicate);
            this.filteredIndexEntries.sort(
                    (e1, e2) -> multiComparator.compare(e1.values, e2.values));

            this.baseIndex = baseIndex;
            this.from = from;
            this.fromInclusionMode = fromInclusionMode;
            this.to = to;
            this.toInclusionMode = toInclusionMode;
            this.nullsModes = nullsModes;
            this.sortModes = sortModes;
        }


        @Override
        public Iterator<LargeInteger> iterator() {
            if (filteredIndexEntries.isEmpty()) {
                return wrapIterator(baseSelection.iterator());
            }

            List<Iterator<LargeInteger>> iterators = new LinkedList<>();
            DiffTableIndexEntry firstEntry = filteredIndexEntries.get(0);
            TableSelection leadingBaseSelection = baseIndex.findMulti(
                    from,
                    fromInclusionMode,
                    firstEntry.values,
                    InclusionMode.INCLUDE,
                    nullsModes,
                    sortModes);
            iterators.add(wrapIterator(leadingBaseSelection.iterator()));
            iterators.add(createMiddleIterator());
            DiffTableIndexEntry lastEntry = filteredIndexEntries.get(filteredIndexEntries.size() - 1);
            iterators.add(Collections.singleton(lastEntry.rowIndex).iterator());
            TableSelection trailingBaseSelection = baseIndex.findMulti(
                    lastEntry.values,
                    InclusionMode.EXCLUDE,
                    to,
                    toInclusionMode,
                    nullsModes,
                    sortModes);
            iterators.add(wrapIterator(trailingBaseSelection.iterator()));
            return ChainedIterator.allOf(iterators);
        }

        private Iterator<LargeInteger> createMiddleIterator() {
            int entryCount = filteredIndexEntries.size();
            return ChainedIterator.over(new IteratorAdapter<>(
                    IntStream.range(0, entryCount - 1).iterator(),
                    i -> {
                        DiffTableIndexEntry beforeEntry = filteredIndexEntries.get(i);
                        DiffTableIndexEntry afterEntry = filteredIndexEntries.get(i + 1);
                        TableSelection betweenBaseSelection = baseIndex.findMulti(
                                beforeEntry.values,
                                InclusionMode.EXCLUDE,
                                afterEntry.values,
                                InclusionMode.INCLUDE,
                                nullsModes,
                                sortModes);
                        return ChainedIterator.of(
                                Collections.singleton(beforeEntry.rowIndex).iterator(),
                                wrapIterator(betweenBaseSelection.iterator()));
                    }));
        }

    }


    private static class UnsortedDiffTableSelection extends AbstractDiffTableSelection {

        public UnsortedDiffTableSelection(
                DiffTableIndexState state,
                TableSelection baseSelection,
                Predicate<ImmutableList<Object>> predicate) {
            super(state, baseSelection, predicate);
        }


        @Override
        public Iterator<LargeInteger> iterator() {
            return ChainedIterator.of(
                    wrapIterator(baseSelection.iterator()),
                    new IteratorAdapter<>(filteredIndexEntries.iterator(), e -> e.rowIndex));
        }

    }


    private static class DiffTableIndexState {

        private final NavigableSet<LargeInteger> deletions;

        private final Set<LargeInteger> updatedRowIndexes;

        private final ArrayList<DiffTableIndexEntry> indexEntries;


        private DiffTableIndexState(
                NavigableSet<LargeInteger> deletions,
                Set<LargeInteger> updatedRowIndexes,
                ArrayList<DiffTableIndexEntry> indexEntries) {
            this.deletions = deletions;
            this.updatedRowIndexes = updatedRowIndexes;
            this.indexEntries = indexEntries;
        }

    }


    private static class DiffTableIndexEntry {

        private final LargeInteger rowIndex;

        private final ImmutableList<Object> values;


        private DiffTableIndexEntry(LargeInteger rowIndex, ImmutableList<Object> values) {
            this.rowIndex = rowIndex;
            this.values = values;
        }

    }

}
