package hu.webarticum.minibase.test.model.suite;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryTestCaseDescription {

    private final String name;

    private final String description;

    private final ImmutableList<String> initQueries;

    private final String query;

    private final ImmutableList<QueryTestResultColumnDescription> columns;

    private final boolean ordered;

    private final ImmutableList<ImmutableList<Object>> expectedResult;

    public QueryTestCaseDescription(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("query") String query,
            @JsonProperty("columns") ImmutableList<QueryTestResultColumnDescription> columns,
            @JsonProperty("ordered") Boolean ordered,
            @JsonProperty("expectedResult") ImmutableList<ImmutableList<Object>> expectedResult) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description != null ? description : "";
        this.initQueries = initQueries != null ? initQueries : ImmutableList.empty();
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.columns = Objects.requireNonNull(columns, "columns must not be null");
        this.ordered = ordered != null ? ordered : true;
        this.expectedResult = Objects.requireNonNull(expectedResult, "expectedResult must not be null");
    }

    @JsonGetter("name")
    public String name() {
        return name;
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
    public ImmutableList<QueryTestResultColumnDescription> columns() {
        return columns;
    }

    @JsonGetter("ordered")
    public boolean ordered() {
        return ordered;
    }

    @JsonGetter("expectedResult")
    public ImmutableList<ImmutableList<Object>> expectedResult() {
        return expectedResult;
    }

}
