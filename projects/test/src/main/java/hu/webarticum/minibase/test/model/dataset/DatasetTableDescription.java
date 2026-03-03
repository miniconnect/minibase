package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class DatasetTableDescription {

    private final String name;

    private final Boolean addDiffLayer;

    private final ImmutableList<DatasetColumnDescription> columns;

    private final ImmutableList<ImmutableList<String>> indexes;

    private final ImmutableList<ImmutableList<Object>> data;

    public DatasetTableDescription(
            @JsonProperty("name") String name,
            @JsonProperty("addDiffLayer") Boolean addDiffLayer,
            @JsonProperty("columns") ImmutableList<DatasetColumnDescription> columns,
            @JsonProperty("indexes") ImmutableList<ImmutableList<String>> indexes,
            @JsonProperty("data") ImmutableList<ImmutableList<Object>> data) {
        this.name = Objects.requireNonNull(name);
        this.addDiffLayer = addDiffLayer != null ? addDiffLayer : false;
        this.columns = columns != null ? columns : ImmutableList.empty();
        this.indexes = indexes != null ? indexes : ImmutableList.empty();
        this.data = Objects.requireNonNull(data);
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("addDiffLayer")
    public Boolean addDiffLayer() {
        return addDiffLayer;
    }

    @JsonGetter("columns")
    public ImmutableList<DatasetColumnDescription> columns() {
        return columns;
    }

    @JsonGetter("indexes")
    public ImmutableList<ImmutableList<String>> indexes() {
        return indexes;
    }

    @JsonGetter("data")
    public ImmutableList<ImmutableList<Object>> data() {
        return data;
    }

}