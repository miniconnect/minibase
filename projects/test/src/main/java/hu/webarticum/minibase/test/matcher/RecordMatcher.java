package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

@FunctionalInterface
public interface RecordMatcher {

    public boolean match(ResultRecord givenRecord, ImmutableList<Object> expectedRow);

}
