package hu.webarticum.minibase.test.model.suite;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryTestResultColumnDescription {

    private final String name;

    private final Class<?> type;

    private final Boolean nullable;

    private final boolean key;

    private final boolean groupKey;

    public QueryTestResultColumnDescription(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("nullable") Boolean nullable,
            @JsonProperty("key") Boolean key,
            @JsonProperty("groupKey") Boolean groupKey) {
        this.name = name;
        this.type = Objects.requireNonNull(type);
        this.nullable = nullable;
        this.key = key == null ? false : key;
        this.groupKey = groupKey != null ? groupKey : false;
    }

    @JsonGetter("name")
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    @JsonGetter("type")
    public Class<?> type() {
        return type;
    }

    @JsonGetter("nullable")
    public Optional<Boolean> nullable() {
        return Optional.ofNullable(nullable);
    }

    @JsonGetter("key")
    public boolean key() {
        return key;
    }

    @JsonGetter("groupKey")
    public boolean groupKey() {
        return groupKey;
    }

}
