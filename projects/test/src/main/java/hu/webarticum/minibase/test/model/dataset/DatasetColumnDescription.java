package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class DatasetColumnDescription {

    private final String name;

    private final Class<?> type;

    private final boolean nullable;

    private final Object defaultValue;

    public DatasetColumnDescription(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("nullable") Boolean nullable,
            @JsonProperty("defaultValue") Object defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.nullable = nullable != null ? nullable : false;
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

    @JsonGetter("nullable")
    public boolean nullable() {
        return nullable;
    }

    @JsonGetter("defaultValue")
    @JsonInclude(Include.NON_NULL)
    public Optional<Object> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

}