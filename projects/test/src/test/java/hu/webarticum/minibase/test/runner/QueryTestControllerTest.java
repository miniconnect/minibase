package hu.webarticum.minibase.test.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.test.matcher.DefaultTableMatcher;
import hu.webarticum.minibase.test.matcher.GroupingDataMatcher;
import hu.webarticum.minibase.test.matcher.OrderedDataMatcher;
import hu.webarticum.minibase.test.matcher.TableMatcher;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.record.ResultTable;

class QueryTestControllerTest {

    private static final String SUITE_LIST_RESOURCE = "hu/webarticum/minibase/test/sample/suite-list.yaml";

    private static final String CASE_1_KEY = "hu/webarticum/minibase/test/sample/suite-1.yaml:case-1";

    private static final String CASE_2_KEY = "hu/webarticum/minibase/test/sample/suite-1.yaml:case-2";

    private static final String SOLE_CASE_KEY = "hu/webarticum/minibase/test/sample/suite-2.yaml:sole-case";

    @Test
    void testLoadedSuites() {
        Map<String, CaseData> collectedCases = Collections.synchronizedMap(new HashMap<>());
        QueryTestController
                .ofResource(SUITE_LIST_RESOURCE)
                .runSuites((path, name, matcher, table, result) -> {
                    collectedCases.put(path + ":" + name, CaseData.of(matcher, table, result));
                });
        assertThat(collectedCases).containsOnlyKeys(CASE_1_KEY, CASE_2_KEY, SOLE_CASE_KEY);
        testCase1(collectedCases.get(CASE_1_KEY));
        testCase2(collectedCases.get(CASE_2_KEY));
        testSoleCase(collectedCases.get(SOLE_CASE_KEY));
    }

    private void testCase1(CaseData caseData) {
        testCase(caseData, GroupingDataMatcher.class, ImmutableList.of(
                ImmutableList.of(LargeInteger.ONE, "xyz")));
    }

    private void testCase2(CaseData caseData) {
        testCase(caseData, OrderedDataMatcher.class, ImmutableList.of(
                ImmutableList.of(LargeInteger.of(99), "", "dolor"),
                ImmutableList.of(LargeInteger.TWO, null, "ipsum"),
                ImmutableList.of(LargeInteger.ONE, "Some description", "lorem")));
    }

    private void testSoleCase(CaseData caseData) {
        testCase(caseData, OrderedDataMatcher.class, ImmutableList.of(
                ImmutableList.of(LargeInteger.ONE, LargeInteger.TWO, LargeInteger.THREE)));
    }

    private void testCase(CaseData caseData, Class<?> matcherType, Iterable<ImmutableList<Object>> result) {
        TableMatcher tableMatcher = caseData.matcher();
        ResultTable givenTable = caseData.table();
        Iterable<ImmutableList<Object>> expectedResult = caseData.result();
        assertThat(expectedResult).containsExactlyElementsOf(result);
        assertThat(tableMatcher).isInstanceOf(DefaultTableMatcher.class);
        assertThat(((DefaultTableMatcher) tableMatcher).dataMatcher()).isInstanceOf(matcherType);
        assertThatCode(() -> tableMatcher.match(givenTable, expectedResult)).doesNotThrowAnyException();
    }

    static class CaseData {

        private final TableMatcher matcher;

        private final ResultTable table;

        private final Iterable<ImmutableList<Object>> result;

        private CaseData(TableMatcher matcher, ResultTable table, Iterable<ImmutableList<Object>> result) {
            this.matcher = matcher;
            this.table = table;
            this.result = result;
        }

        public static CaseData of(TableMatcher matcher, ResultTable table, Iterable<ImmutableList<Object>> result) {
            return new CaseData(matcher, table, result);
        }

        public TableMatcher matcher() {
            return matcher;
        }

        public ResultTable table() {
            return table;
        }

        public Iterable<ImmutableList<Object>> result() {
            return result;
        }

    }

}
