package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class DatasetTableDescription {

    private final String name;

    private final ImmutableList<DatasetColumnDescription> columns;

    private final ImmutableList<ImmutableList<Object>> data;

    public DatasetTableDescription(
            @JsonProperty("name") String name,
            @JsonProperty("columns") ImmutableList<DatasetColumnDescription> columns,
            @JsonProperty("data") ImmutableList<ImmutableList<Object>> data) {
        this.name = Objects.requireNonNull(name);
        this.columns = columns != null ? columns : ImmutableList.empty();
        this.data = Objects.requireNonNull(data);
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("columns")
    public ImmutableList<DatasetColumnDescription> columns() {
        return columns;
    }

    @JsonGetter("data")
    public ImmutableList<ImmutableList<Object>> data() {
        return data;
    }

}