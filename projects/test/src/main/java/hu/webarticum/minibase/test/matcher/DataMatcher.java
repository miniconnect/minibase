package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

@FunctionalInterface
public interface DataMatcher {

    public boolean match(Iterable<ResultRecord> givenRecords, Iterable<ImmutableList<Object>> expectedData);

}
