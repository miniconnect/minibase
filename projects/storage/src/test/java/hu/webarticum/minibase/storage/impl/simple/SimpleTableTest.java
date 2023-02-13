package hu.webarticum.minibase.storage.impl.simple;

import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.impl.AbstractWritableTableTest;
import hu.webarticum.miniconnect.lang.ImmutableList;

class SimpleTableTest extends AbstractWritableTableTest {

    @Override
    protected SimpleTable tableFrom(
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableList<ImmutableList<Object>> content) {
        return simpleTableFrom(columnNames, columnDefinitions, content);
    }

}
