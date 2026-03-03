package hu.webarticum.minibase.test.runner;

import hu.webarticum.minibase.test.matcher.TableMatcher;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

@FunctionalInterface
public interface QueryTestCaseCallback {

    public void accept(
            String resourcePath,
            String caseName,
            TableMatcher tableMatcher,
            ResultTable givenTable,
            Iterable<ImmutableList<Object>> expectedResult);

}
