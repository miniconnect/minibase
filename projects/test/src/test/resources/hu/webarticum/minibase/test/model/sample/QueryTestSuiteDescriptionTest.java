package hu.webarticum.minibase.test.model.suite;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

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
        assertThat(case1.description()).isEqualTo("This is a test case");
        assertThat(case1.initQueries()).isEmpty();
        assertThat(case1.query()).isEqualTo("SELECT id, label FROM tbl_1 LIMIT 1");
        assertThat(case1.columns()).hasSize(2);
        assertThat(case1.columns().get(0).expectedName()).isNotPresent();
        assertThat(case1.columns().get(0).expectedType()).isNotPresent();
        assertThat(case1.columns().get(1).expectedName()).isPresent();
        assertThat(case1.columns().get(1).expectedType()).isNotPresent();
        // TODO: comparison settigns
        assertThat(case1.expectedResult()).containsExactly(ImmutableList.of(1, "xyz"));

        QueryTestCaseDescription case2 = suite.cases().get(1);
        assertThat(case2.description()).isEqualTo("This is another test case");
        assertThat(case2.initQueries()).containsExactly(
                "INSERT INTO tbl_1(id, label) VALUES(1, 'Hello')",
                "INSERT INTO tbl_2(id, name) VALUES(99, 'Lorem Ipsum')");
        assertThat(case2.query()).isEqualTo("SELECT id FROM tbl_2 LIMIT 10");
        assertThat(case2.columns()).hasSize(1);
        assertThat(case2.columns().get(0).expectedName()).isPresent();
        assertThat(case2.columns().get(0).expectedType()).isPresent();
        // TODO: comparison settigns
        assertThat(case2.expectedResult()).containsExactly(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"),
                ImmutableList.of(99, "dolor"));
    }

    @Test
    void testMappingOfSuite2() throws IOException {
        QueryTestSuiteDescription suite = loadSuite2();
        // TODO
    }

    private QueryTestSuiteDescription loadSuite1() throws IOException {
        return loadSuite(SUITE_1_RESOURCE, "suite 1 resource stream");
    }

    private QueryTestSuiteDescription loadSuite2() throws IOException {
        return loadSuite(SUITE_2_RESOURCE, "suite 2 resource stream");
    }

    private QueryTestSuiteDescription loadSuite(String resourcePath, String resourceDescription) throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(resourcePath, resourceDescription)) {
            return mapper.readValue(in, QueryTestSuiteDescription.class);
        }
    }

}
