package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.ResultRecord;

public class DefaultRecordMatcher implements RecordMatcher {

    private final ImmutableList<FieldMatcher> fieldMatchers;

    private DefaultRecordMatcher(ImmutableList<FieldMatcher> fieldMatchers) {
        this.fieldMatchers = fieldMatchers;
    }

    public static DefaultRecordMatcher of(ImmutableList<FieldMatcher> fieldMatchers) {
        return new DefaultRecordMatcher(fieldMatchers);
    }

    @Override
    public boolean match(ResultRecord givenRecord, ImmutableList<Object> expectedRow) {
        int length = fieldMatchers.size();
        if (length != expectedRow.size()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            FieldMatcher fieldMatcher = fieldMatchers.get(i);
            ResultField field = givenRecord.get(i);
            Object expectedValue = expectedRow.get(i);
            if (!fieldMatcher.match(field, expectedValue)) {
                return false;
            }
        }
        return true;
    }

}
