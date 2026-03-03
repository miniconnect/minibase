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
    public boolean isMatching(ResultRecord givenRecord, ImmutableList<Object> expectedRow) throws Exception {
        int matcherLength = fieldMatchers.size();
        int expectedLength = expectedRow.size();
        if (matcherLength != expectedLength) {
            return false;
        }
        int recordLength = givenRecord.row().size();
        if (recordLength != expectedLength) {
            return false;
        }
        for (int i = 0; i < matcherLength; i++) {
            FieldMatcher fieldMatcher = fieldMatchers.get(i);
            ResultField field = givenRecord.get(i);
            Object expectedValue = expectedRow.get(i);
            if (!fieldMatcher.isMatching(field, expectedValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void match(ResultRecord givenRecord, ImmutableList<Object> expectedRow) throws Exception {
        int matcherLength = fieldMatchers.size();
        int expectedLength = expectedRow.size();
        if (matcherLength != expectedLength) {
            throw new MatchFailedException("matcher length: " + matcherLength + " != expected row length: " + expectedLength);
        }
        int recordLength = givenRecord.row().size();
        if (recordLength != expectedLength) {
            throw new MatchFailedException("record length: " + recordLength + " != expected row length: " + expectedLength);
        }
        for (int i = 0; i < matcherLength; i++) {
            FieldMatcher fieldMatcher = fieldMatchers.get(i);
            ResultField field = givenRecord.get(i);
            Object expectedValue = expectedRow.get(i);
            try {
                fieldMatcher.match(field, expectedValue);
            } catch (Exception e) {
                throw MatchFailedException.prefix("at column " + i + ": ", e);
            }
        }
    }

}
