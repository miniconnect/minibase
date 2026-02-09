package hu.webarticum.minibase.test.model.fixture;

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

class QueryTestFixtureTest {

    private final static String RESOURCE_PATH = "hu/webarticum/minibase/test/model/test-fixture.yaml";

    @Test
    void testMapping() throws IOException {
        QueryTestFixture fixture = loadFixture();
        assertThat(fixture.description()).isEqualTo("This is a test test fixture");
        assertThat(fixture.datasetResource()).isEqualTo("test-dataset.yaml");
        assertThat(fixture.initQueries()).containsExactly("USE db");
        assertThat(fixture.cases()).hasSize(2);

        QueryTestCase case1 = fixture.cases().get(0);
        assertThat(case1.description()).isEqualTo("This is a test test case");
        assertThat(case1.initQueries()).isEmpty();
        assertThat(case1.query()).isEqualTo("SELECT id, label FROM tbl_1 LIMIT 1");
        assertThat(case1.columns()).hasSize(2);
        assertThat(case1.columns().get(0).expectedName()).isNotPresent();
        assertThat(case1.columns().get(0).expectedType()).isNotPresent();
        assertThat(case1.columns().get(1).expectedName()).isPresent();
        assertThat(case1.columns().get(1).expectedType()).isNotPresent();
        // TODO: comparison settigns
        assertThat(case1.expectedResult()).containsExactly(ImmutableList.of(1, "xyz"));

        QueryTestCase case2 = fixture.cases().get(1);
        assertThat(case2.description()).isEqualTo("This is another test test case");
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

    private QueryTestFixture loadFixture() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openFixtureInputStream()) {
            return mapper.readValue(in, QueryTestFixture.class);
        }
    }

    private InputStream openFixtureInputStream() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
        assertThat(in).as("fixture resource stream").isNotNull();
        return in;
    }

}
