package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

@FunctionalInterface
public interface TableMatcher {

    public void match(ResultTable givenTable, Iterable<ImmutableList<Object>> expectedData) throws Exception;

}
