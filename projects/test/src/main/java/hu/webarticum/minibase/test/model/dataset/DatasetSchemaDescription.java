package hu.webarticum.minibase.test.model.dataset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class DatasetSchemaDescription {

    private final String name;

    private final ImmutableList<DatasetTableDescription> tables;

    public DatasetSchemaDescription(
            @JsonProperty("name") String name,
            @JsonProperty("tables") ImmutableList<DatasetTableDescription> tables) {
        this.name = name;
        this.tables = tables;
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("tables")
    public ImmutableList<DatasetTableDescription> tables() {
        return tables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("tables", tables)
                .build();
    }

}