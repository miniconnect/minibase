package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

public class DefaultTableMatcher implements TableMatcher {

    private final TableHeaderMatcher tableHeaderMatcher;

    private final DataMatcher dataMatcher;

    private DefaultTableMatcher(TableHeaderMatcher tableHeaderMatcher, DataMatcher dataMatcher) {
        this.tableHeaderMatcher = tableHeaderMatcher;
        this.dataMatcher = dataMatcher;
    }

    public static DefaultTableMatcher of(TableHeaderMatcher tableHeaderMatcher, DataMatcher dataMatcher) {
        return new DefaultTableMatcher(tableHeaderMatcher, dataMatcher);
    }

    public TableHeaderMatcher tableHeaderMatcher() {
        return tableHeaderMatcher;
    }

    public DataMatcher dataMatcher() {
        return dataMatcher;
    }

    @Override
    public boolean match(ResultTable givenTable, Iterable<ImmutableList<Object>> expectedData) {
        return
                tableHeaderMatcher.match(givenTable.resultSet().columnHeaders()) &&
                dataMatcher.match(givenTable, expectedData);
    }

}
