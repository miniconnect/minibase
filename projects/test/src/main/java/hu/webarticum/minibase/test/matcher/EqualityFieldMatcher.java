package hu.webarticum.minibase.test.matcher;

import java.util.Objects;

import hu.webarticum.miniconnect.record.ResultField;

public class EqualityFieldMatcher implements FieldMatcher {

    private static final EqualityFieldMatcher INSTANCE = new EqualityFieldMatcher();

    public static EqualityFieldMatcher instance() {
        return INSTANCE;
    }

    @Override
    public boolean match(ResultField field, Object expectedValue) {
        return Objects.equals(field.get(), expectedValue);
    }

}
