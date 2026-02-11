package hu.webarticum.minibase.test.model.suite;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.test.model.AbstractResourceBasedTest;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class QueryTestSuiteDescriptionTest extends AbstractResourceBasedTest {

    private final static String SUITE_1_RESOURCE = "hu/webarticum/minibase/test/model/sample/suite-1.yaml";

    private final static String SUITE_2_RESOURCE = "hu/webarticum/minibase/test/model/sample/suite-2.yaml";

    @Test
    void testMappingOfSuite1() throws IOException {
        QueryTestSuiteDescription suite = loadSuite1();
        assertThat(suite.description()).isEqualTo("This is a sample test suite");
        assertThat(suite.datasetResource()).isEqualTo("sample-dataset.yaml");
        assertThat(suite.initQueries()).containsExactly("USE db");
        assertThat(suite.cases()).hasSize(2);

        QueryTestCaseDescription case1 = suite.cases().get(0);
        assertThat(case1.name()).isEqualTo("case-1");
        assertThat(case1.description()).isEqualTo("This is a test case");
        assertThat(case1.initQueries()).isEmpty();
        assertThat(case1.query()).isEqualTo("SELECT id, label FROM tbl_1 ORDER BY id LIMIT 1");
        assertThat(case1.columns()).hasSize(2);
        assertThat(case1.columns().get(0).name()).isNotPresent();
        assertThat(case1.columns().get(0).type()).isEqualTo(String.class);
        assertThat(case1.columns().get(0).nullable()).isNotPresent();
        assertThat(case1.columns().get(1).name()).contains("label");
        assertThat(case1.columns().get(1).type()).isEqualTo(String.class);
        assertThat(case1.columns().get(1).nullable()).contains(true);
        // TODO: comparison settigns
        assertThat(case1.expectedResult()).containsExactly(ImmutableList.of(1, "xyz"));

        QueryTestCaseDescription case2 = suite.cases().get(1);
        assertThat(case2.name()).isEqualTo("case-2");
        assertThat(case2.description()).isEqualTo("This is another test case");
        assertThat(case2.initQueries()).containsExactly(
                "INSERT INTO tbl_2(id, name, description) VALUES(99, 'lorem', '')");
        assertThat(case2.query()).isEqualTo("SELECT id, description, name FROM tbl_2 ORDER BY id DESC");
        assertThat(case2.columns()).hasSize(3);
        assertThat(case2.columns().get(0).name()).contains("id");
        assertThat(case2.columns().get(0).type()).isEqualTo(LargeInteger.class);
        assertThat(case2.columns().get(0).nullable()).contains(false);
        assertThat(case2.columns().get(1).name()).contains("description");
        assertThat(case2.columns().get(1).type()).isEqualTo(String.class);
        assertThat(case2.columns().get(1).nullable()).contains(true);
        assertThat(case2.columns().get(2).name()).contains("name");
        assertThat(case2.columns().get(2).type()).isEqualTo(String.class);
        assertThat(case2.columns().get(2).nullable()).contains(false);
        // TODO: comparison settigns
        assertThat(case2.expectedResult()).containsExactly(
                ImmutableList.of(99, "", "dolor"),
                ImmutableList.of(2, null, "ipsum"),
                ImmutableList.of(1, "Some description", "lorem"));
    }

    @Test
    void testMappingOfSuite2() throws IOException {
        QueryTestSuiteDescription suite = loadSuite2();
        assertThat(suite.description()).isEqualTo("This is another sample test suite");
        assertThat(suite.datasetResource()).isEqualTo("sample-dataset.yaml");
        assertThat(suite.initQueries()).isEmpty();
        assertThat(suite.cases()).hasSize(1);

        QueryTestCaseDescription soleCase = suite.cases().get(0);
        assertThat(soleCase.name()).isEqualTo("sole-case");
        assertThat(soleCase.description()).isEmpty();
        assertThat(soleCase.initQueries()).isEmpty();
        assertThat(soleCase.query()).isEqualTo("SELECT 1 AS one, 2 AS two, 3 AS three");
        assertThat(soleCase.columns()).hasSize(3);
        assertThat(soleCase.columns().get(0).name()).contains("one");
        assertThat(soleCase.columns().get(0).type()).isEqualTo(LargeInteger.class);
        assertThat(soleCase.columns().get(0).nullable()).contains(false);
        assertThat(soleCase.columns().get(1).name()).contains("two");
        assertThat(soleCase.columns().get(1).type()).isEqualTo(LargeInteger.class);
        assertThat(soleCase.columns().get(1).nullable()).contains(false);
        assertThat(soleCase.columns().get(2).name()).contains("three");
        assertThat(soleCase.columns().get(2).type()).isEqualTo(LargeInteger.class);
        assertThat(soleCase.columns().get(2).nullable()).contains(false);
        // TODO: comparison settigns
        assertThat(soleCase.expectedResult()).containsExactly(ImmutableList.of(1, 2, 3));
    }

    private QueryTestSuiteDescription loadSuite1() throws IOException {
        return loadYaml(SUITE_1_RESOURCE, "suite 1 resource stream", QueryTestSuiteDescription.class);
    }

    private QueryTestSuiteDescription loadSuite2() throws IOException {
        return loadYaml(SUITE_2_RESOURCE, "suite 2 resource stream", QueryTestSuiteDescription.class);
    }

}
