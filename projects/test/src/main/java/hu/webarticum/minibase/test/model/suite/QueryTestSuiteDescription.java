package hu.webarticum.minibase.test.model.suite;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryTestSuiteDescription {

    private final String description;

    private final String datasetResource;

    private final ImmutableList<String> initQueries;

    private final ImmutableList<QueryTestCaseDescription> cases;

    public QueryTestSuiteDescription(
            @JsonProperty("description") String description,
            @JsonProperty("datasetResource") String datasetResource,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("cases") ImmutableList<QueryTestCaseDescription> cases) {
        this.description = description != null ? description : "";
        this.datasetResource = Objects.requireNonNull(datasetResource, "datasetResource must not be null");
        this.initQueries = initQueries != null ? initQueries : ImmutableList.empty();
        this.cases = Objects.requireNonNull(cases, "cases must not be null");
    }

    @JsonGetter("description")
    public String description() {
        return description;
    }

    @JsonGetter("datasetResource")
    public String datasetResource() {
        return datasetResource;
    }

    @JsonGetter("initQueries")
    public ImmutableList<String> initQueries() {
        return initQueries;
    }

    @JsonGetter("cases")
    public ImmutableList<QueryTestCaseDescription> cases() {
        return cases;
    }

}
