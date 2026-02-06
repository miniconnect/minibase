package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class OrderedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    public OrderedDataMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    public boolean match(Iterable<ResultRecord> recordIterable, Iterable<ImmutableList<Object>> expectedData) {
        Iterator<ImmutableList<Object>> expectedDataIterator = expectedData.iterator();
        for (ResultRecord record : recordIterable) {
            if (!expectedDataIterator.hasNext()) {
                return false;
            }
            ImmutableList<Object> expectedRow = expectedDataIterator.next();
            recordMatcher.match(record, expectedRow);
        }
        return !expectedDataIterator.hasNext();
    }

}