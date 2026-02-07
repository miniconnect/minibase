package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.record.ResultField;

@FunctionalInterface
public interface FieldMatcher {

    public boolean match(ResultField givenField, Object expectedValue);

}
