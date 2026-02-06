package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.ResultRecord;

public class DefaultRecordMatcher implements RecordMatcher {

    private final ImmutableList<FieldMatcher> fieldMatchers;

    public DefaultRecordMatcher(ImmutableList<FieldMatcher> fieldMatchers) {
        this.fieldMatchers = fieldMatchers;
    }

    public boolean match(ResultRecord record, ImmutableList<Object> expectedRow) {
        int length = fieldMatchers.size();
        for (int i = 0; i < length; i++) {
            FieldMatcher fieldMatcher = fieldMatchers.get(i);
            ResultField field = record.get(i);
            Object expectedValue = expectedRow.get(i);
            if (!fieldMatcher.match(field, expectedValue)) {
                return false;
            }
        }
        return true;
    }

}
