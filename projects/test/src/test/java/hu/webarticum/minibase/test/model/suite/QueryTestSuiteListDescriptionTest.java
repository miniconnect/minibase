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

import hu.webarticum.minibase.test.model.AbstractResourceBasedTest;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

class QueryTestSuiteListDescriptionTest extends AbstractResourceBasedTest {

    private final static String SUITE_LIST_RESOURCE = "hu/webarticum/minibase/test/model/sample/suite-list.yaml";

    @Test
    void testMapping() throws IOException {
        QueryTestSuiteListDescription suiteList = loadSuiteList();
        assertThat(suiteList.suites()).containsExactly(
                "suite-1.yaml",
                "suite-2.yaml");
    }

    private QueryTestSuiteListDescription loadSuiteList() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(SUITE_LIST_RESOURCE, "suite list resource stream")) {
            return mapper.readValue(in, QueryTestSuiteListDescription.class);
        }
    }

}
