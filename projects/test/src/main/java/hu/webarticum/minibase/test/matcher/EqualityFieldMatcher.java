package hu.webarticum.minibase.test.matcher;

import java.util.Objects;

import hu.webarticum.miniconnect.record.ResultField;

public class EqualityFieldMatcher implements FieldMatcher {

    private static final EqualityFieldMatcher INSTANCE = new EqualityFieldMatcher();

    private EqualityFieldMatcher() {
        // use instance()
    }

    public static EqualityFieldMatcher instance() {
        return INSTANCE;
    }

    @Override
    public boolean match(ResultField givenField, Object expectedValue) {
        return Objects.equals(givenField.get(), expectedValue);
    }

}
