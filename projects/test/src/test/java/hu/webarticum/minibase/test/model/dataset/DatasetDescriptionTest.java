package hu.webarticum.minibase.test.model.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

class DatasetDescriptionTest {

    private final static String RESOURCE_PATH = "hu/webarticum/minibase/test/model/dataset/test-dataset.yaml";

    @Test
    void testMapping() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        DatasetDescription datasetDescription;
        try (InputStream in = openFixtureInputStream()) {
            datasetDescription = mapper.readValue(in, DatasetDescription.class);
        }

        // TODO
        assertThat(datasetDescription.schemas()).hasSize(1);

    }

    private InputStream openFixtureInputStream() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
        assertThat(in).as("dataset resource stream").isNotNull();
        return in;
    }

}
