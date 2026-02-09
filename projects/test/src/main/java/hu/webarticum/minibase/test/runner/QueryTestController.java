package hu.webarticum.minibase.test.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.minibase.test.model.suite.QueryTestSuiteListDescription;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public class QueryTestController {

    private final String suiteListResourcePath;

    public QueryTestController(String suiteListResourcePath) {
        this.suiteListResourcePath = suiteListResourcePath;
    }

    public void runSuites(QueryTestCaseCallback callback) {
        try {
			runSuitesInternal(callback);
		} catch (IOException e) {
		    throw new UncheckedIOException("Unexpected " + e.getClass().getSimpleName(), e);
		}
    }

    private void runSuitesInternal(QueryTestCaseCallback callback) throws IOException {

        // TODO
        loadYaml(suiteListResourcePath, QueryTestSuiteListDescription.class);

    }

    private <T> T loadYaml(String resourcePath, Class<T> type) throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(resourcePath)) {
            return mapper.readValue(in, type);
        }
    }

    protected InputStream openResourceInputStream(String resourcePath) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("YAML resource stream is null: " + resourcePath);
        }
        return in;
    }

}
