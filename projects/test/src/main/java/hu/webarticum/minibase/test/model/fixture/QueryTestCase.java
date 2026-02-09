package hu.webarticum.minibase.test.model.fixture;

import java.util.Objects;

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
        this.description = description != null ? description : "";
        this.initQueries = initQueries != null ? initQueries : ImmutableList.empty();
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.columns = Objects.requireNonNull(columns, "columns must not be null");
        // TODO: field: comparison settigns
        this.expectedResult = Objects.requireNonNull(expectedResult, "expectedResult must not be null");
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
