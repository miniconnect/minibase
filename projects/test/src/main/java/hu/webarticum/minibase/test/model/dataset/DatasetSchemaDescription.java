package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class DatasetSchemaDescription {

    private final String name;

    private final ImmutableList<DatasetTableDescription> tables;

    public DatasetSchemaDescription(
            @JsonProperty("name") String name,
            @JsonProperty("tables") ImmutableList<DatasetTableDescription> tables) {
        this.name = Objects.requireNonNull(name);
        this.tables = tables != null ? tables : ImmutableList.empty();
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("tables")
    public ImmutableList<DatasetTableDescription> tables() {
        return tables;
    }

}