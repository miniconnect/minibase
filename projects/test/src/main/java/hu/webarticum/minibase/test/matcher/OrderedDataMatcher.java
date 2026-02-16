package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class OrderedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    private OrderedDataMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    public static OrderedDataMatcher of(RecordMatcher recordMatcher) {
        return new OrderedDataMatcher(recordMatcher);
    }

    @Override
    public void match(Iterable<ResultRecord> givenRecords, Iterable<ImmutableList<Object>> expectedData) throws Exception {
        Iterator<ImmutableList<Object>> expectedDataIterator = expectedData.iterator();
        int i = 0;
        for (ResultRecord record : givenRecords) {
            if (!expectedDataIterator.hasNext()) {
                throw new MatchFailedException("too many records");
            }
            ImmutableList<Object> expectedRow = expectedDataIterator.next();
            try {
                recordMatcher.match(record, expectedRow);
            } catch (Exception e) {
                throw MatchFailedException.prefix("at row " + i + ": ", e);
            }
            i++;
        }
        if (expectedDataIterator.hasNext()) {
            throw new MatchFailedException("too few records");
        }
    }

}