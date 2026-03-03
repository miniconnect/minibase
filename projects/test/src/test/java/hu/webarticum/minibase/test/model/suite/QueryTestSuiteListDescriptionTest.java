package hu.webarticum.minibase.test.model.suite;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.test.model.AbstractResourceBasedTest;

class QueryTestSuiteListDescriptionTest extends AbstractResourceBasedTest {

    private final static String SUITE_LIST_RESOURCE = "hu/webarticum/minibase/test/sample/suite-list.yaml";

    @Test
    void testMapping() throws IOException {
        QueryTestSuiteListDescription suiteList = loadSuiteList();
        assertThat(suiteList.suites()).containsExactly(
                "suite-1.yaml",
                "suite-2.yaml");
    }

    private QueryTestSuiteListDescription loadSuiteList() throws IOException {
        return loadYaml(SUITE_LIST_RESOURCE, "suite list resource stream", QueryTestSuiteListDescription.class);
    }

}
