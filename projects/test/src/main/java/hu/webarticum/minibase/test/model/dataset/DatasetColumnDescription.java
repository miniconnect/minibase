package hu.webarticum.minibase.test.model.dataset;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class DatasetColumnDescription {

    private final String name;

    private final Class<?> type;

    private final boolean nullable;

    private final boolean unique;

    private final boolean autoIncremented;

    private final ImmutableList<Object> enumValues;

    private final Object defaultValue;

    public DatasetColumnDescription(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("nullable") Boolean nullable,
            @JsonProperty("unique") Boolean unique,
            @JsonProperty("autoIncremented") Boolean autoIncremented,
            @JsonProperty("enumValues") ImmutableList<Object> enumValues,
            @JsonProperty("defaultValue") Object defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.nullable = nullable != null ? nullable : false;
        this.unique = unique != null ? unique : false;
        this.autoIncremented = autoIncremented != null ? autoIncremented : false;
        this.enumValues = enumValues;
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

    @JsonGetter("unique")
    public boolean unique() {
        return unique;
    }

    @JsonGetter("autoIncremented")
    public boolean autoIncremented() {
        return autoIncremented;
    }

    @JsonGetter("enumValues")
    @JsonInclude(Include.NON_NULL)
    public Optional<ImmutableList<Object>> enumValues() {
        return Optional.ofNullable(enumValues);
    }

    @JsonGetter("defaultValue")
    @JsonInclude(Include.NON_NULL)
    public Optional<Object> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

}