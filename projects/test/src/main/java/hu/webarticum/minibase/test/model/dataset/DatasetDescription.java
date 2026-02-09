package hu.webarticum.minibase.test.model.dataset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class DatasetDescription {

    private final ImmutableList<DatasetSchemaDescription> schemas;

    public DatasetDescription(
            @JsonProperty("schemas") ImmutableList<DatasetSchemaDescription> schemas) {
        this.schemas = schemas != null ? schemas : ImmutableList.empty();
    }

    @JsonGetter("schemas")
    public ImmutableList<DatasetSchemaDescription> schemas() {
        return schemas;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("schemas", schemas)
                .build();
    }

}