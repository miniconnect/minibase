package hu.webarticum.minibase.test.model.fixture;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryTestFixture {

    private final String description;

    private final String datasetPath;

    private final ImmutableList<String> initQueries;

    private final ImmutableList<QueryTestCase> cases;

    public QueryTestFixture(
            @JsonProperty("description") String description,
            @JsonProperty("datasetPath") String datasetPath,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("cases") ImmutableList<QueryTestCase> cases) {
        this.description = description;
        this.datasetPath = datasetPath;
        this.initQueries = initQueries;
        this.cases = cases;
    }

    @JsonGetter("description")
    public String description() {
        return description;
    }

    @JsonGetter("datasetPath")
    public String datasetPath() {
        return datasetPath;
    }

    @JsonGetter("initQueries")
    public ImmutableList<String> initQueries() {
        return initQueries;
    }

    @JsonGetter("cases")
    public ImmutableList<QueryTestCase> cases() {
        return cases;
    }

}
