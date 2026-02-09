package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class DatasetTableDescription {

    private final String name;

    private final ImmutableList<DatasetColumnDescription> columns;

    private final String dataResource;

    public DatasetTableDescription(
            @JsonProperty("name") String name,
            @JsonProperty("columns") ImmutableList<DatasetColumnDescription> columns,
            @JsonProperty("dataResource") String dataResource) {
        this.name = Objects.requireNonNull(name);
        this.columns = columns != null ? columns : ImmutableList.empty();
        this.dataResource = Objects.requireNonNull(dataResource);
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("columns")
    public ImmutableList<DatasetColumnDescription> columns() {
        return columns;
    }

    @JsonGetter("dataResource")
    public String dataResource() {
        return dataResource;
    }

}