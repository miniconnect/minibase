package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

@FunctionalInterface
public interface RecordIterableMatcher {

    public boolean match(Iterable<ResultRecord> recordIterable, ImmutableList<ImmutableList<Object>> expectedData);

}
