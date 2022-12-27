package hu.webarticum.minibase.query.execution.impl.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.expression.ColumnExpression;
import hu.webarticum.minibase.query.expression.ColumnParameter;
import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.minibase.query.expression.FixedTypeExpression;
import hu.webarticum.minibase.query.expression.Parameter;
import hu.webarticum.minibase.query.expression.VariableExpression;
import hu.webarticum.minibase.query.expression.VariableParameter;
import hu.webarticum.minibase.query.query.JoinType;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SelectQuery;
import hu.webarticum.minibase.query.query.SelectQuery.ExpressionSelectItem;
import hu.webarticum.minibase.query.query.SelectQuery.JoinItem;
import hu.webarticum.minibase.query.query.SelectQuery.OrderByItem;
import hu.webarticum.minibase.query.query.SelectQuery.SelectItem;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.minibase.query.query.SelectQuery.WildcardSelectItem;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.impl.simple.MultiComparator;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniValue;
import hu.webarticum.miniconnect.api.MiniValueDefinition;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.impl.result.StoredResultSetData;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.record.translator.JavaTranslator;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;
import hu.webarticum.miniconnect.record.type.StandardValueType;

public class SelectExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (SelectQuery) query);
    }

    private MiniResult executeInternal(StorageAccess storageAccess, SessionState state, SelectQuery selectQuery) {
        LinkedHashMap<String, TableEntry> tableEntries = collectTableEntries(selectQuery, storageAccess, state);
        
        List<SelectItemEntry> selectItemEntries = collectSelectItemEntries(
                selectQuery, tableEntries, storageAccess, state);
        List<OrderByEntry> orderByEntries = collectOrderByEntries(selectQuery, selectItemEntries, tableEntries);
        Set<String> uniqueOrderedAliases = new HashSet<>();
        List<OrderByEntry> normalizedOrderByEntries = normalizeOrderByEntries(
                tableEntries, orderByEntries, uniqueOrderedAliases);

        LinkedHashMap<String, TableEntry> reorderedTableEntries = applyOrderByToTableEntries(
                tableEntries, uniqueOrderedAliases, normalizedOrderByEntries);
        
        ImmutableList<MiniColumnHeader> columnHeaders = selectItemEntries.stream()
                .map(e -> columnHeaderOf(e, reorderedTableEntries))
                .collect(ImmutableList.createCollector());

        try {
            addFilters(selectQuery.where(), reorderedTableEntries, state);
        } catch (IncompatibleFiltersException e) {
            return new StoredResult(new StoredResultSetData(columnHeaders, ImmutableList.empty()));
        }

        LargeInteger limit = selectQuery.limit();

        List<Map<String, LargeInteger>> joinedRowIndices = collectRows(
                reorderedTableEntries, normalizedOrderByEntries, limit, state);
        
        ImmutableList<ImmutableList<MiniValue>> data = joinedRowIndices.stream()
                .map(r -> selectRow(r, selectItemEntries, reorderedTableEntries, state))
                .collect(ImmutableList.createCollector());
        
        return new StoredResult(new StoredResultSetData(columnHeaders, data));
    }

    private LinkedHashMap<String, TableEntry> collectTableEntries(
            SelectQuery selectQuery, StorageAccess storageAccess, SessionState state) {
        LinkedHashMap<String, TableEntry> result = new LinkedHashMap<>();
        addTableInfo(
                result,
                selectQuery.tableAlias(),
                selectQuery.schemaName(),
                selectQuery.tableName(),
                null,
                storageAccess,
                state);
        for (JoinItem joinItem : selectQuery.join()) {
            addTableInfo(
                    result,
                    joinItem.targetTableAlias(),
                    joinItem.targetSchemaName(),
                    joinItem.targetTableName(),
                    joinItem,
                    storageAccess,
                    state);
        }
        return result;
    }
    
    private List<OrderByEntry> normalizeOrderByEntries(
            LinkedHashMap<String, TableEntry> tableEntries,
            List<OrderByEntry> orderByEntries,
            Set<String> uniqueOrderedAliasesOut) {
        List<OrderByEntry> result = new ArrayList<>();
        for (OrderByEntry orderByEntry : orderByEntries) {
            String tableAlias = orderByEntry.tableAlias;
            if (uniqueOrderedAliasesOut.contains(tableAlias)) {
                continue;
            }
            
            String fieldName = orderByEntry.fieldName;
            ColumnDefinition definition = tableEntries.get(tableAlias).table.columns().get(fieldName).definition();
            if (definition.isUnique() && !definition.isNullable()) {
                uniqueOrderedAliasesOut.add(tableAlias);
            }
            result.add(orderByEntry);
        }
        return result;
    }

    private LinkedHashMap<String, TableEntry> applyOrderByToTableEntries(
            LinkedHashMap<String, TableEntry> tableEntries,
            Set<String> uniqueOrderedAliases,
            List<OrderByEntry> orderByEntries) {
        if (orderByEntries.isEmpty()) {
            return tableEntries;
        }

        List<String> prefixOrderableTableNames = collectPrefixOrderableTableNames(uniqueOrderedAliases, orderByEntries);
        prefixOrderableTableNames = matchPrefixOrderableTableNames(tableEntries, prefixOrderableTableNames);

        if (prefixOrderableTableNames.isEmpty()) {
            return tableEntries;
        }
        
        boolean allPreorderable = prefixOrderableTableNames.size() == orderByEntries.size();
        
        LinkedHashMap<String, TableEntry> result = new LinkedHashMap<>();
        
        addTableEntriesFollowingAliases(tableEntries, prefixOrderableTableNames, true, result);
        List<String> preorderableAliases = new ArrayList<>(result.keySet());

        String baseAlias = tableEntries.keySet().iterator().next();
        List<String> connectJoinPath = exploreJoinPath(tableEntries, preorderableAliases, baseAlias);
        if (connectJoinPath == null) {
            throw PredefinedError.OTHER_ERROR.toException();
        }
        addTableEntriesFollowingAliases(tableEntries, connectJoinPath, allPreorderable, result);

        List<String> tailJoinPath = new ArrayList<>(tableEntries.keySet());
        tailJoinPath.removeAll(result.keySet());
        addTableEntriesFollowingAliases(tableEntries, tailJoinPath, allPreorderable, result);

        return result;
    }
    
    private List<String> collectPrefixOrderableTableNames(
            Set<String> uniqueOrderedAliases, List<OrderByEntry> orderByEntries) {
        List<String> result = new ArrayList<>();
        String previousAlias = null;
        boolean aborted = false;
        for (OrderByEntry orderByEntry : orderByEntries) {
            int foundIndex = result.indexOf(orderByEntry.tableAlias);
            if (foundIndex == -1) {
                result.add(orderByEntry.tableAlias);
            } else if (!orderByEntry.tableAlias.equals(previousAlias)) {
                result = new ArrayList<>(result.subList(0, foundIndex));
                aborted = true;
                break;
            }
            previousAlias = orderByEntry.tableAlias;
        }
        int to = result.size();
        if (!aborted) {
            to--;
        }
        for (int i = 0; i < to; i++) {
            if (!uniqueOrderedAliases.contains(orderByEntries.get(i).tableAlias)) {
                return new ArrayList<>(result.subList(0, i));
            }
        }
        return result;
    }

    private List<String> matchPrefixOrderableTableNames(
            LinkedHashMap<String, TableEntry> tableEntries, List<String> prefixOrderableTableNames) {
        List<String> result = new ArrayList<>();
        Set<String> possibleNexts = collectInitialPossibleNextTableNames(tableEntries);
        for (String tableName : prefixOrderableTableNames) {
            if (!possibleNexts.contains(tableName)) {
                return result;
            }
            
            result.add(tableName);
            possibleNexts.clear();
            for (String tableAlias : tableEntries.keySet()) {
                if (findJoinBetween(tableEntries, tableName, tableAlias) != null) {
                    possibleNexts.add(tableAlias);
                }
            }
        }
        return result;
    }
    
    private Set<String> collectInitialPossibleNextTableNames(LinkedHashMap<String, TableEntry> tableEntries) {
        Set<String> result = new HashSet<>();
        String baseTableName = tableEntries.keySet().iterator().next();
        result.add(baseTableName);
        int previousSize;
        do {
            previousSize = result.size();
            for (TableEntry tableEntry : tableEntries.values()) {
                String nextTableAlias = extractAdditionalInitialPossible(result, tableEntry);
                if (nextTableAlias != null) {
                    result.add(nextTableAlias);
                }
            }
        } while (result.size() > previousSize);
        return result;
    }

    private String extractAdditionalInitialPossible(Set<String> result, TableEntry tableEntry) {
        JoinItem joinItem = tableEntry.joinItem;
        if (joinItem == null || joinItem.joinType() != JoinType.INNER) {
            return null;
        } else if (result.contains(joinItem.sourceTableAlias())) {
            return joinItem.targetTableAlias();
        } else {
            return null;
        }
    }
    
    private void addTableEntriesFollowingAliases(
            LinkedHashMap<String, TableEntry> tableEntries,
            Collection<String> tableAliases,
            boolean preorderable,
            LinkedHashMap<String, TableEntry> result) {
        for (String tableAlias : tableAliases) {
            TableEntry newTableEntry = detectTableEntryBetweenAny(tableEntries, result.keySet(), tableAlias, preorderable);
            result.put(tableAlias, newTableEntry);
        }
    }

    private TableEntry detectTableEntryBetweenAny(
            LinkedHashMap<String, TableEntry> tableEntries,
            Collection<String> previousAliases,
            String nextAlias,
            boolean preorderable) {
        TableEntry originalTableEntry = tableEntries.get(nextAlias);
        JoinItem joinItem = flipJoinItemIfNecessary(
                detectJoinBetweenAny(tableEntries, previousAliases, nextAlias),
                originalTableEntry,
                nextAlias);
        TableEntry newTableEntry = new TableEntry(
                originalTableEntry.schemaName, originalTableEntry.table, joinItem, preorderable);
        newTableEntry.valueTranslators.putAll(originalTableEntry.valueTranslators);
        newTableEntry.subFilter.putAll(originalTableEntry.subFilter);
        return newTableEntry;
    }
    
    private JoinItem flipJoinItemIfNecessary(JoinItem joinItem, TableEntry targetEntry, String targetAlias) {
        if (joinItem == null) {
            return null;
        } else if (joinItem.targetTableAlias().equals(targetAlias)) {
            return joinItem;
        }
        
        return new JoinItem(
                joinItem.joinType(),
                targetEntry.schemaName,
                targetEntry.table.name(),
                joinItem.sourceTableAlias(),
                joinItem.sourceFieldName(),
                joinItem.targetTableAlias(),
                joinItem.targetFieldName());
    }

    private JoinItem detectJoinBetweenAny(
            LinkedHashMap<String, TableEntry> tableEntries, Collection<String> previousAliases, String nextAlias) {
        for (String previousAlias: previousAliases) {
            JoinItem joinItem = findJoinBetween(tableEntries, previousAlias, nextAlias);
            if (joinItem != null) {
                return joinItem;
            }
        }
        return null;
    }
    
    private JoinItem findJoinBetween(
            LinkedHashMap<String, TableEntry> tableEntries, String previousAlias, String nextAlias) {
        TableEntry targetTableEntry = tableEntries.get(nextAlias);
        JoinItem targetJoinItem = targetTableEntry.joinItem;
        if (targetJoinItem != null && targetJoinItem.sourceTableAlias().equals(previousAlias)) {
            return targetJoinItem;
        }

        TableEntry sourceTableEntry = tableEntries.get(previousAlias);
        JoinItem sourceJoinItem = sourceTableEntry.joinItem;
        if (
                sourceJoinItem != null &&
                sourceJoinItem.joinType() == JoinType.INNER &&
                sourceJoinItem.sourceTableAlias().equals(nextAlias)) {
            return sourceJoinItem;
        }

        return null;
    }
    
    private List<String> exploreJoinPath(
            LinkedHashMap<String, TableEntry> tableEntries, List<String> preorderableAliases, String baseAlias) {
        if (preorderableAliases.contains(baseAlias)) {
            return new LinkedList<>();
        }
        
        List<String> innerJoinedAliases = tableEntries.values().stream()
                .filter(e -> isInnerJoinFrom(e, baseAlias))
                .map(e -> e.joinItem.targetTableAlias())
                .collect(Collectors.toList());
        
        for (String targetAlias : innerJoinedAliases) {
            List<String> subJoinPath = exploreJoinPath(tableEntries, preorderableAliases, targetAlias);
            if (subJoinPath != null) {
                subJoinPath.add(targetAlias);
                return subJoinPath;
            }
        }
        
        return null;
    }
    
    private boolean isInnerJoinFrom(TableEntry tableEntry, String baseAlias) {
        JoinItem joinItem = tableEntry.joinItem;
        return
                joinItem != null &&
                joinItem.joinType() == JoinType.INNER &&
                joinItem.sourceTableAlias().equals(baseAlias);
    }
    
    private List<SelectItemEntry> collectSelectItemEntries(
            SelectQuery selectQuery,
            LinkedHashMap<String, TableEntry> tableEntries,
            StorageAccess storageAccess,
            SessionState state) {
        List<SelectItemEntry> result = new ArrayList<>();
        ImmutableList<SelectItem> querySelectItems = selectQuery.selectItems();
        for (SelectItem querySelectItem : querySelectItems) {
            addSelectItemEntries(result, querySelectItem, tableEntries, storageAccess, state);
        }
        return result;
    }
    
    private List<OrderByEntry> collectOrderByEntries(
            SelectQuery selectQuery,
            List<SelectItemEntry> selectItemEntries,
            LinkedHashMap<String, TableEntry> tableEntries) {
        List<OrderByEntry> result = new ArrayList<>();
        ImmutableList<OrderByItem> orderByItems = selectQuery.orderBy();
        for (OrderByItem orderByItem : orderByItems) {
            result.add(toOrderByEntry(orderByItem, selectItemEntries, tableEntries));
        }
        return result;
    }
    
    private void addTableInfo(
            LinkedHashMap<String, TableEntry> tableEntries,
            String alias,
            String schemaName,
            String tableName,
            JoinItem joinItem,
            StorageAccess storageAccess,
            SessionState state) {
        if (schemaName == null) {
            schemaName = state.getCurrentSchema();
        }
        if (schemaName == null) {
            throw PredefinedError.SCHEMA_NOT_SELECTED.toException();
        }
        
        Schema schema = storageAccess.schemas().get(schemaName);
        if (schema == null) {
            throw PredefinedError.SCHEMA_NOT_FOUND.toException(schemaName);
        }
        
        Table table = schema.tables().get(tableName);
        if (table == null) {
            throw PredefinedError.TABLE_NOT_FOUND.toException(tableName);
        }
        
        if (alias == null) {
            alias = tableName;
        }
        
        if (tableEntries.containsKey(alias)) {
            throw PredefinedError.TABLE_ALIAS_DUPLICATED.toException(alias);
        }
        
        tableEntries.put(alias, new TableEntry(schemaName, table, joinItem, false));

        checkJoinItem(joinItem, tableEntries);
    }
    
    private void checkJoinItem(JoinItem joinItem, Map<String, TableEntry> tableEntries) {
        if (joinItem == null) {
            return;
        }
        
        Table sourceTable = tableEntries.get(joinItem.sourceTableAlias()).table;
        String sourceFieldName = joinItem.sourceFieldName();
        checkColumn(sourceTable, sourceFieldName);

        Table targetTable = tableEntries.get(joinItem.targetTableAlias()).table;
        String targetFieldName = joinItem.targetFieldName();
        checkColumn(targetTable, targetFieldName);
    }

    private void addFilters(
            ImmutableList<WhereItem> whereItems,
            Map<String, TableEntry> tableEntries,
            SessionState state) {
        for (WhereItem whereItem : whereItems) {
            addFilter(whereItem, tableEntries, state);
        }
    }
    
    private void addFilter(WhereItem whereItem, Map<String, TableEntry> tableEntries, SessionState state) {
        String tableName = whereItem.tableName();
        if (tableName == null) {
            tableName = tableEntries.keySet().iterator().next();
        }
        
        TableEntry tableEntry = tableEntries.get(tableName);
        if (tableEntry == null) {
            throw PredefinedError.TABLE_NOT_FOUND.toException(tableName);
        }

        String fieldName = whereItem.fieldName();
        checkColumn(tableEntry.table, fieldName);
        
        Object value = whereItem.value();
        ColumnDefinition columnDefinition = tableEntry.table.columns().get(fieldName).definition();
        
        TableQueryUtil.applyFilterValue(tableEntry.subFilter, fieldName, value, columnDefinition, state);
    }
    
    private void addSelectItemEntries(
            List<SelectItemEntry> selectItemEntries,
            SelectItem querySelectItem,
            LinkedHashMap<String, TableEntry> tableEntries,
            StorageAccess storageAccess,
            SessionState state) {
        if (querySelectItem instanceof WildcardSelectItem) {
            WildcardSelectItem wildcardSelectItem = (WildcardSelectItem) querySelectItem;
            String wildcardTableAlias = wildcardSelectItem.tableAlias();
            addSelectItemEntriesForWildcard(selectItemEntries, wildcardTableAlias, tableEntries, storageAccess);
            return;
        }

        if (querySelectItem instanceof ExpressionSelectItem) {
            ExpressionSelectItem expressionSelectItem = (ExpressionSelectItem) querySelectItem;
            SelectItemEntry selectItemEntry = createSelectItemEntryForExpression(
                    expressionSelectItem, tableEntries, storageAccess, state);
            selectItemEntries.add(selectItemEntry);
            return;
        }
        
        throw new IllegalArgumentException("Unknown select item type: " + querySelectItem.getClass());
    }

    private SelectItemEntry createSelectItemEntryForExpression(
            ExpressionSelectItem expressionSelectItem,
            LinkedHashMap<String, TableEntry> tableEntries,
            StorageAccess storageAccess,
            SessionState state) {
        Expression expression = expressionSelectItem.expression();
        if (expression instanceof FixedTypeExpression) {
            Class<?> type = ((FixedTypeExpression) expression).type();
            return createSelectItemEntryForFixedType(expressionSelectItem, type, true);
        } else if (expression instanceof VariableExpression) {
            String variableName = ((VariableExpression) expression).variableParameter().variableName();
            Object variableValue = state.getUserVariable(variableName);
            boolean isNull = variableValue == null;
            Class<?> type = isNull ? Void.class : variableValue.getClass();
            return createSelectItemEntryForFixedType(expressionSelectItem, type, isNull);
        } else if (expression instanceof ColumnExpression) {
            return createSelectItemEntryForColumnExpression(expressionSelectItem, tableEntries);
        } else {
            throw new IllegalArgumentException("Expression type without type information: " + expression.getClass());
        }
    }
    
    private SelectItemEntry createSelectItemEntryForFixedType(
            ExpressionSelectItem expressionSelectItem, Class<?> type, boolean nullable) {
        ColumnDefinition fakeColumnDefinition = new SimpleColumnDefinition(type, nullable);
        ValueTranslator valueTranslator = createValueTranslator(fakeColumnDefinition);
        return new SelectItemEntry(expressionSelectItem, valueTranslator, fakeColumnDefinition);
    }
    
    private SelectItemEntry createSelectItemEntryForColumnExpression(
            ExpressionSelectItem expressionSelectItem, LinkedHashMap<String, TableEntry> tableEntries) {
        ColumnExpression columnExpression = (ColumnExpression) expressionSelectItem.expression();
        ColumnParameter columnParameter = columnExpression.columnParameter();
        
        String tableAias = columnParameter.tableAlias();
        String columnName = columnParameter.columnName();
        
        if (tableAias == null) {
            tableAias = tableEntries.keySet().iterator().next();
        } else if (!tableEntries.containsKey(tableAias)) {
            throw PredefinedError.TABLE_NOT_FOUND.toException(tableAias);
        }
        TableEntry tableEntry = tableEntries.get(tableAias);
        checkColumn(tableEntry.table, columnName);
        
        ValueTranslator valueTranslator = getValueTranslator(tableEntry, columnName);
        ColumnDefinition columnDefinition = tableEntry.table.columns().get(columnName).definition();
        return new SelectItemEntry(expressionSelectItem, valueTranslator, columnDefinition);
    }
    
    private void addSelectItemEntriesForWildcard(
            List<SelectItemEntry> selectItemEntries,
            String tableName,
            LinkedHashMap<String, TableEntry> tableEntries,
            StorageAccess storageAccess) {
        if (tableName == null) {
            for (String infoTableName : tableEntries.keySet()) {
                addSelectItemEntriesForWildcard(selectItemEntries, infoTableName, tableEntries, storageAccess);
            }
            return;
        }
        
        TableEntry tableEntry = tableEntries.get(tableName);
        Table table = tableEntry.table;
        NamedResourceStore<Column> columns = table.columns();
        for (String columnName : columns.names()) {
            Expression columnExpression = new ColumnExpression(tableName, columnName);
            SelectItem columnSelectItem = new ExpressionSelectItem(columnExpression, columnName);
            ValueTranslator valueTranslator = getValueTranslator(tableEntry, columnName);
            ColumnDefinition columnDefinition = columns.get(columnName).definition();
            selectItemEntries.add(new SelectItemEntry(columnSelectItem, valueTranslator, columnDefinition));
        }
    }
    
    private OrderByEntry toOrderByEntry(
            OrderByItem orderByItem,
            List<SelectItemEntry> selectItemEntries,
            LinkedHashMap<String, TableEntry> tableEntries) {
        Integer position = orderByItem.position();
        if (orderByItem.position() != null) {
            if (position < 1 || position > selectItemEntries.size()) {
                throw PredefinedError.COLUMN_POSITION_INVALID.toException(position);
            }
            SelectItemEntry selectItemEntry = selectItemEntries.get(position - 1);
            ColumnParameter columnParameter = columnParameterOf(selectItemEntry);
            if (columnParameter == null) {
                throw new IllegalArgumentException("Currently, only column based ordering is supported");
            }
            return new OrderByEntry(
                    columnParameter.tableAlias(),
                    columnParameter.columnName(),
                    orderByItem.ascOrder(),
                    orderByItem.nullsOrderMode());
        }
        
        String tableName = orderByItem.tableName();
        String fieldName = orderByItem.fieldName();
        
        if (tableName == null) {
            ColumnParameter matchingColumnParameter = findMatchingColumnParameter(selectItemEntries, fieldName);
            if (matchingColumnParameter != null) {
                return new OrderByEntry(
                        matchingColumnParameter.tableAlias(),
                        matchingColumnParameter.columnName(),
                        orderByItem.ascOrder(),
                        orderByItem.nullsOrderMode());
            } else {
                tableName = tableEntries.keySet().iterator().next();
            }
        }

        TableEntry tableEntry = tableEntries.get(tableName);
        if (tableEntry == null) {
            throw PredefinedError.TABLE_NOT_FOUND.toException(tableName);
        }
        checkColumn(tableEntry.table, fieldName);
        
        return new OrderByEntry(tableName, fieldName, orderByItem.ascOrder(), orderByItem.nullsOrderMode());
    }
    
    private ColumnParameter findMatchingColumnParameter(List<SelectItemEntry> selectItemEntries, String fieldName) {
        for (SelectItemEntry selectItemEntry : selectItemEntries) {
            ColumnParameter columnParameter = columnParameterOf(selectItemEntry);
            if (columnParameter == null) {
                continue;
            }
            ExpressionSelectItem expressionSelectItem = (ExpressionSelectItem) selectItemEntry.selectItem;
            if (expressionSelectItem.alias().equals(fieldName)) {
                return columnParameter;
            }
        }
        return null;
    }
    
    private ColumnParameter columnParameterOf(SelectItemEntry selectItemEntry) {
        SelectItem selectItem = selectItemEntry.selectItem;
        if (!(selectItem instanceof ExpressionSelectItem)) {
            return null;
        }
        Expression expression = ((ExpressionSelectItem) selectItem).expression();
        if (!(expression instanceof ColumnExpression)) {
            return null;
        }
        return ((ColumnExpression) expression).columnParameter();
    }
    
    private void checkColumn(Table table, String columnName) {
        if (!table.columns().contains(columnName)) {
            throw PredefinedError.COLUMN_NOT_FOUND.toException(table.name(), columnName);
        }
    }
    
    private ValueTranslator getValueTranslator(TableEntry tableEntry, String fieldName) {
        return tableEntry.valueTranslators.computeIfAbsent(
                fieldName, k -> createValueTranslator(tableEntry.table, fieldName));
    }

    private ValueTranslator createValueTranslator(Table table, String fieldName) {
        checkColumn(table, fieldName);
        ColumnDefinition columnDefinition = table.columns().get(fieldName).definition();
        return createValueTranslator(columnDefinition);
    }

    private ValueTranslator createValueTranslator(ColumnDefinition columnDefinition) {
        Class<?> clazz = columnDefinition.clazz();
        Optional<StandardValueType> optional = StandardValueType.forClazz(clazz);
        if (optional.isPresent()) {
            return optional.get().defaultTranslator();
        }
        return JavaTranslator.of(clazz);
    }
    
    private MiniColumnHeader columnHeaderOf(SelectItemEntry selectItemEntry, Map<String, TableEntry> tableEntries) {
        MiniValueDefinition valueDefinition = selectItemEntry.valueTranslator.definition();
        SelectItem selectItem = selectItemEntry.selectItem;
        if (!(selectItem instanceof ExpressionSelectItem)) {
            throw new IllegalArgumentException("Not a column expression");
        }
        ExpressionSelectItem expressionSelectItem = (ExpressionSelectItem) selectItem;
        String tableAlias = tableAliasOf(expressionSelectItem);
        String fieldAlias = expressionSelectItem.alias();
        if (fieldAlias == null) {
            fieldAlias = expressionSelectItem.expression().automaticName();
        }
        boolean isNullable =
                selectItemEntry.columnDefinition.isNullable() ||
                (tableAlias != null && isTransitivelyLeftJoined(tableAlias, tableEntries));
        return new StoredColumnHeader(fieldAlias, isNullable, valueDefinition);
    }
    
    private String tableAliasOf(ExpressionSelectItem expressionSelectItem) {
        Expression expression = expressionSelectItem.expression();
        if (!(expression instanceof ColumnExpression)) {
            return null;
        }
        
        return ((ColumnExpression) expression).columnParameter().tableAlias();
    }

    private boolean isTransitivelyLeftJoined(String tableAlias, Map<String, TableEntry> tableEntries) {
        return isTransitivelyLeftJoined(tableAlias, tableEntries, 0);
    }
    
    private boolean isTransitivelyLeftJoined(String tableAlias, Map<String, TableEntry> tableEntries, int i) {
        JoinItem joinItem = tableEntries.get(tableAlias).joinItem;
        if (joinItem == null) {
            return false;
        } else if (joinItem.joinType() == JoinType.LEFT_OUTER) {
            return true;
        } else {
            return isTransitivelyLeftJoined(joinItem.sourceTableAlias(), tableEntries, i + 1);
        }
    }
    
    private ImmutableList<MiniValue> selectRow(
            Map<String, LargeInteger> joinedRow,
            List<SelectItemEntry> selectItemEntries,
            Map<String, TableEntry> tableEntries,
            SessionState state) {
        List<MiniValue> resultBuilder = new ArrayList<>(selectItemEntries.size());
        for (SelectItemEntry selectItemEntry : selectItemEntries) {
            MiniValue miniValue = selectRowValue(joinedRow, selectItemEntry, tableEntries, state);
            resultBuilder.add(miniValue);
        }
        return ImmutableList.fromCollection(resultBuilder);
    }
    
    private MiniValue selectRowValue(
            Map<String, LargeInteger> joinedRow,
            SelectItemEntry selectItemEntry,
            Map<String, TableEntry> tableEntries,
            SessionState state) {
        SelectItem selectItem = selectItemEntry.selectItem;
        if (!(selectItem instanceof ExpressionSelectItem)) {
            throw new IllegalArgumentException("Not an expression item: " + selectItem.getClass());
        }

        Expression expression = ((ExpressionSelectItem) selectItem).expression();
        ImmutableList<Parameter> parameters = expression.parameters();
        ImmutableMap<Parameter, Object> parameterValues = parameters.assign(
                p -> selectExpressionParameter(joinedRow, tableEntries, state, p));
        Object value = expression.evaluate(parameterValues);
        return selectItemEntry.valueTranslator.encodeFully(value);
    }
    
    private Object selectExpressionParameter(
            Map<String, LargeInteger> joinedRow,
            Map<String, TableEntry> tableEntries,
            SessionState state,
            Parameter parameter) {
        if (parameter instanceof VariableParameter) {
            return selectExpressionVariableParameter(state, (VariableParameter) parameter);
        } else if (parameter instanceof ColumnParameter) {
            return selectExpressionColumnParameter(joinedRow, tableEntries, (ColumnParameter) parameter);
        } else {
            throw new IllegalArgumentException("Unknown parameter type: " + parameter.getClass());
        }
    }
    
    private Object selectExpressionVariableParameter(SessionState state, VariableParameter variableParameter) {
        return state.getUserVariable(variableParameter.variableName());
    }
    
    private Object selectExpressionColumnParameter(
            Map<String, LargeInteger> joinedRow,
            Map<String, TableEntry> tableEntries,
            ColumnParameter columnParameter) {
        String tableAlias = columnParameter.tableAlias();
        if (tableAlias == null) {
            tableAlias = tableEntries.keySet().iterator().next();
        }
        String columnName = columnParameter.columnName();
        LargeInteger rowIndex = joinedRow.get(tableAlias);
        Object value = null;
        if (rowIndex != null) {
            TableEntry tableEntry = tableEntries.get(tableAlias);
            value = tableEntry.table.row(rowIndex).get(columnName);
        }
        return value;
    }

    private List<Map<String, LargeInteger>> collectRows(
            LinkedHashMap<String, TableEntry> tableEntries,
            List<OrderByEntry> orderByEntries,
            LargeInteger limit,
            SessionState state) {
        List<String> remainingTableAliasList = new ArrayList<>(tableEntries.keySet());
        Map<String, LargeInteger> joinedPrefix = new HashMap<>();
        List<Map<String, LargeInteger>> result = new ArrayList<>();
        collectRowsFromNextTable(
                result, null, remainingTableAliasList, orderByEntries, joinedPrefix, limit, tableEntries, state);
        return result;
    }
    
    private void collectRowsFromNextTable(
            List<Map<String, LargeInteger>> result,
            String previousAlias,
            List<String> remainingTableAliasList,
            List<OrderByEntry> remainingOrderByEntries,
            Map<String, LargeInteger> joinedPrefix,
            LargeInteger limit,
            LinkedHashMap<String, TableEntry> tableEntries,
            SessionState state) {
        boolean isLeaf = remainingTableAliasList.size() == 1;
        String tableAlias = remainingTableAliasList.get(0);
        TableEntry tableEntry = tableEntries.get(tableAlias);
        Map<String, Object> subFilter = new HashMap<>(tableEntry.subFilter);
        
        boolean baseIsNull = false;
        if (tableEntry.joinItem != null) {
            String sourceTableAlias = tableEntry.joinItem.sourceTableAlias();
            Table sourceTable = tableEntries.get(sourceTableAlias).table;
            LargeInteger rowIndex = joinedPrefix.get(sourceTableAlias);
            if (rowIndex != null) {
                String sourceFieldName = tableEntry.joinItem.sourceFieldName();
                String targetFieldName = tableEntry.joinItem.targetFieldName();
                Object joinValue = sourceTable.row(rowIndex).get(sourceFieldName);
                ColumnDefinition columnDefinition = tableEntry.table.columns().get(targetFieldName).definition();
                try {
                    TableQueryUtil.applyFilterValue(subFilter, targetFieldName, joinValue, columnDefinition, state);
                } catch (IncompatibleFiltersException e) {
                    return;
                }
            } else {
                baseIsNull = true;
            }
        }
        List<String> subRemainingTableAliasList = remainingTableAliasList.subList(1, remainingTableAliasList.size());
        List<OrderByEntry> currentOrderByEntries = new ArrayList<>();
        List<OrderByEntry> subRemainingOrderByEntries = new ArrayList<>();
        if (tableEntry.preorderable) {
            for (OrderByEntry orderByEntry : remainingOrderByEntries) {
                if (!orderByEntry.tableAlias.equals(tableAlias)) {
                    break;
                }
                currentOrderByEntries.add(orderByEntry);
            }
            subRemainingOrderByEntries.addAll(
                    remainingOrderByEntries.subList(currentOrderByEntries.size(), remainingOrderByEntries.size()));
        }
        
        int intLimit = Integer.MAX_VALUE;
        if (limit != null) {
            try {
                intLimit = limit.intValueExact();
            } catch (ArithmeticException e) {
                // XXX: falls back to Integer.MAX_VALUE
            }
        }
        
        boolean found = false;
        if (previousAlias == null || !baseIsNull) {
            int previousSize = result.size();
            
            List<Map<String, LargeInteger>> subResult = new ArrayList<>();
            
            Iterator<LargeInteger> rowIndexIterator;
            if (tableEntry.preorderable) {
                rowIndexIterator = TableQueryUtil.filterRows(
                        tableEntry.table, subFilter, currentOrderByEntries, limit);
            } else {
                rowIndexIterator = TableQueryUtil.filterRows(
                        tableEntry.table, subFilter, Collections.emptyList(), limit);
            }
            found = rowIndexIterator.hasNext();
            while (rowIndexIterator.hasNext()) {
                LargeInteger rowIndex = rowIndexIterator.next();
                Map<String, LargeInteger> joinedRow = new HashMap<>(joinedPrefix);
                joinedRow.put(tableAlias, rowIndex);
                if (isLeaf) {
                    subResult.add(joinedRow);
                } else {
                    collectRowsFromNextTable(
                            subResult,
                            tableAlias,
                            subRemainingTableAliasList,
                            subRemainingOrderByEntries,
                            joinedRow,
                            limit,
                            tableEntries,
                            state);
                }
                if (
                        limit != null &&
                        previousSize + subResult.size() >= intLimit) {
                    break;
                }
            }

            boolean rootNonPreorderable =
                    !tableEntry.preorderable &&
                    (previousAlias == null || tableEntries.get(previousAlias).preorderable);
            if (rootNonPreorderable && !remainingOrderByEntries.isEmpty()) {
                Function<String, Table> tableResolver = alias -> tableEntries.get(alias).table;
                MultiComparator multiComparator = TableQueryUtil.createMultiComparator(
                        remainingOrderByEntries, tableResolver);
                Comparator<Map<String, LargeInteger>> rowIndexComparator = (r1, r2) -> multiComparator.compare(
                        TableQueryUtil.extractOrderValues(remainingOrderByEntries, tableResolver, r1::get),
                        TableQueryUtil.extractOrderValues(remainingOrderByEntries, tableResolver, r2::get));
                subResult.sort(rowIndexComparator);
                if (limit != null) {
                    subResult = subResult.subList(0, intLimit);
                }
            }
            
            result.addAll(subResult);
        }
        if (!found && tableEntry.joinItem != null && tableEntry.joinItem.joinType() == JoinType.LEFT_OUTER) {
            Map<String, LargeInteger> joinedRow = new HashMap<>(joinedPrefix);
            joinedRow.put(tableAlias, null);
            if (isLeaf) {
                result.add(joinedRow);
            } else {
                collectRowsFromNextTable(
                        result,
                        tableAlias,
                        subRemainingTableAliasList,
                        subRemainingOrderByEntries,
                        joinedRow,
                        limit,
                        tableEntries,
                        state);
            }
        }
    }
    
}
