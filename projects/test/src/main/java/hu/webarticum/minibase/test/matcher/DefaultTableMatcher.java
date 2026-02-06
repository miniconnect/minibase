package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

public class DefaultTableMatcher implements TableMatcher {

    private final DataMatcher dataMatcher;

    private final TableHeaderMatcher tableHeaderMatcher;

    public DefaultTableMatcher(DataMatcher dataMatcher, TableHeaderMatcher tableHeaderMatcher) {
        this.dataMatcher = dataMatcher;
        this.tableHeaderMatcher = tableHeaderMatcher;
    }

    public boolean match(ResultTable table, Iterable<ImmutableList<Object>> expectedData) {
        return
                tableHeaderMatcher.match(table.resultSet().columnHeaders()) &&
                dataMatcher.match(table, expectedData);
    }

}
