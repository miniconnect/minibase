package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.record.ResultField;

@FunctionalInterface
public interface FieldMatcher {

    public boolean isMatching(ResultField givenField, Object expectedValue) throws Exception;

    public default void match(ResultField givenField, Object expectedValue) throws Exception {
        if (!isMatching(givenField, expectedValue)) {
            throw new MatchFailedException(givenField.get() + " != " + expectedValue);
        }
    }

}
