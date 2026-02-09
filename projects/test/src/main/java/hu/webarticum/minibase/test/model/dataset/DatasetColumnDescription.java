package hu.webarticum.minibase.test.model.dataset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ToStringBuilder;;

public class DatasetColumnDescription {

    private final String name;

    private final Class<?> type;

    private final Object defaultValue;

    public DatasetColumnDescription( // NOSONAR: many parameter is OK
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("defaultValue") Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("type")
    public Class<?> type() {
        return type;
    }

    @JsonGetter("defaultValue")
    @JsonInclude(Include.NON_NULL)
    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("type", type)
                .add("defaultValue", defaultValue)
                .build();
    }

}