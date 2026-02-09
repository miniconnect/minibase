package hu.webarticum.minibase.test.model.fixture;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryColumnDefinition {

    private final String expectedName;

    private final Class<?> expectedType;

    public QueryColumnDefinition(
            @JsonProperty("expectedName") String expectedName,
            @JsonProperty("expectedType") Class<?> expectedType) {
        this.expectedName = expectedName;
        this.expectedType = expectedType;
    }

    @JsonGetter("expectedName")
    public Optional<String> expectedName() {
        return Optional.ofNullable(expectedName);
    }

    @JsonGetter("expectedType")
    public Optional<Class<?>> expectedType() {
        return Optional.ofNullable(expectedType);
    }

}
