package hu.webarticum.minibase.storage.api;

import hu.webarticum.miniconnect.lang.ImmutableList;

public interface SingleColumnTableIndex extends TableIndex {

    public String columnName();

    @Override
    public default ImmutableList<String> columnNames() {
        return ImmutableList.of(columnName());
    }

    @Override
    public default int width() {
        return 1;
    }

    @Override
    public default TableSelection findMulti(
            ImmutableList<?> from,
            InclusionMode fromInclusionMode,
            ImmutableList<?> to,
            InclusionMode toInclusionMode,
            ImmutableList<NullsMode> nullsModes,
            ImmutableList<SortMode> sortModes) {
        if (from != null && from.size() > 1) {
            throw new IllegalArgumentException(
                    "Parameter 'from' must not contain multiple values");
        }
        if (to != null && to.size() > 1) {
            throw new IllegalArgumentException(
                    "Parameter 'to' must not contain multiple values");
        }
        if (nullsModes.size() > 1) {
            throw new IllegalArgumentException(
                    "Parameter 'nullsModes' must not contain multiple values");
        }
        if (sortModes.size() > 1) {
            throw new IllegalArgumentException(
                    "Parameter 'sortModes' must not contain multiple values");
        }

        Object fromValue = from == null ? null : from.get(0);
        Object toValue = to == null ? null : to.get(0);
        NullsMode nullsMode = nullsModes.isEmpty() ? NullsMode.WITH_NULLS : nullsModes.get(0);
        SortMode sortMode = sortModes.isEmpty() ? SortMode.UNSORTED : sortModes.get(0);

        // FIXME: hotfix: currently, single value indices expects low-to-high parameter order
        Object lowValue = sortMode.isAsc() ? fromValue : toValue;
        InclusionMode lowInclusionMode = sortMode.isAsc() ? fromInclusionMode : toInclusionMode;
        Object highValue = sortMode.isAsc() ? toValue : fromValue;
        InclusionMode highInclusionMode = sortMode.isAsc() ? toInclusionMode : fromInclusionMode;

        return find(lowValue, lowInclusionMode, highValue, highInclusionMode, nullsMode, sortMode);
    }

    @Override
    public TableSelection find(
            Object from,
            InclusionMode fromInclusionMode,
            Object to,
            InclusionMode toInclusionMode,
            NullsMode nullsMode,
            SortMode sortMode);

}
