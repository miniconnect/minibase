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

    private final static String RESOURCE_PATH = "hu/webarticum/minibase/test/model/test-dataset.yaml";

    @Test
    void testMapping() throws IOException {
        DatasetDescription datasetDescription = loadDatasetDescription();

        // TODO
        assertThat(datasetDescription.schemas()).hasSize(1);
        assertThat(datasetDescription.schemas().get(0).tables()).hasSize(2);

    }

    private DatasetDescription loadDatasetDescription() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openDatasetInputStream()) {
            return mapper.readValue(in, DatasetDescription.class);
        }
    }

    private InputStream openDatasetInputStream() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH);
        assertThat(in).as("dataset resource stream").isNotNull();
        return in;
    }

}
