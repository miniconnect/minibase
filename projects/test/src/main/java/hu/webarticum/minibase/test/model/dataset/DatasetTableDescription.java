package hu.webarticum.minibase.test.model.dataset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class DatasetTableDescription {

    private final String name;

    private final Boolean writeable;

    private final ImmutableList<DatasetColumnDescription> columns;

    public DatasetTableDescription(
            @JsonProperty("name") String name,
            @JsonProperty("writeable") Boolean writeable,
            @JsonProperty("columns") ImmutableList<DatasetColumnDescription> columns) {
        this.name = name;
        this.writeable = writeable;
        this.columns = columns;
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("writeable")
    @JsonInclude(Include.NON_NULL)
    public Boolean writeable() {
        return writeable;
    }

    @JsonGetter("columns")
    public ImmutableList<DatasetColumnDescription> columns() {
        return columns;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("writeable", writeable)
                .add("columns", columns)
                .build();
    }

}