package hu.webarticum.minibase.test.model.fixture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

class QueryTestFixtureTest {

    private final static String RESOURCE_PATH = "hu/webarticum/minibase/test/model/fixture/test-fixture.yaml";

    @Test
    void testMapping() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        QueryTestFixture fixture;
        try (InputStream in = openFixtureInputStream()) {
            fixture = mapper.readValue(in, QueryTestFixture.class);
        }

        // TODO
        assertThat(fixture.description()).isEqualTo("This is a test test fixture");

    }

    private InputStream openFixtureInputStream() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
        assertThat(in).as("fixture resource stream").isNotNull();
        return in;
    }

}
