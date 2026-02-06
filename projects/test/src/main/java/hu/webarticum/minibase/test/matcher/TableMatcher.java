package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

@FunctionalInterface
public interface TableMatcher {

    public boolean match(ResultTable table, ImmutableList<ImmutableList<Object>> expectedData);

}
