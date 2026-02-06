package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class OrderedRecordIterableMatcher implements RecordIterableMatcher {

    private final RecordMatcher recordMatcher;

    public OrderedRecordIterableMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    public boolean match(Iterable<ResultRecord> recordIterable, ImmutableList<ImmutableList<Object>> expectedData) {
        Iterator<ImmutableList<Object>> expectedDataIterator = expectedData.iterator();
        for (ResultRecord record : recordIterable) {
            if (!expectedDataIterator.hasNext()) {
                return false;
            }
            ImmutableList<Object> expectedValues = expectedDataIterator.next();
            recordMatcher.match(record, expectedValues);
        }
        return !expectedDataIterator.hasNext();
    }

}