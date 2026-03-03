package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

@FunctionalInterface
public interface RecordMatcher {

    public boolean isMatching(ResultRecord givenRecord, ImmutableList<Object> expectedRow) throws Exception;

    public default void match(ResultRecord givenRecord, ImmutableList<Object> expectedRow) throws Exception {
        if (!isMatching(givenRecord, expectedRow)) {
            throw new MatchFailedException("unmatching record: " + givenRecord.getAll() + " != " + expectedRow);
        }
    }

}
