package hu.webarticum.minibase.test.model.fixture;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryTestCase {

    private final String description;

    private final ImmutableList<String> initQueries;

    private final String query;

    private final ImmutableList<QueryColumnDefinition> columns;

    // TODO: field: comparison settigns

    private final ImmutableList<ImmutableList<Object>> expectedResult;

    public QueryTestCase(
            @JsonProperty("description") String description,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("query") String query,
            @JsonProperty("columns") ImmutableList<QueryColumnDefinition> columns,
            // TODO: field: comparison settigns
            @JsonProperty("expectedResult") ImmutableList<ImmutableList<Object>> expectedResult) {
        this.description = description;
        this.initQueries = initQueries;
        this.query = query;
        this.columns = columns;
        // TODO: field: comparison settigns
        this.expectedResult = expectedResult;
    }

    @JsonGetter("description")
    public String description() {
        return description;
    }

    @JsonGetter("initQueries")
    public ImmutableList<String> initQueries() {
        return initQueries;
    }

    @JsonGetter("query")
    public String query() {
        return query;
    }

    @JsonGetter("columns")
    public ImmutableList<QueryColumnDefinition> columns() {
        return columns;
    }

    // TODO: field: comparison settigns

    @JsonGetter("expectedResult")
    public ImmutableList<ImmutableList<Object>> expectedResult() {
        return expectedResult;
    }

}
