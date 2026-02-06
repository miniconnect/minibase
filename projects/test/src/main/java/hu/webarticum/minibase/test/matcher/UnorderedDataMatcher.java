package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class UnorderedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    public UnorderedDataMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    @Override
    public boolean match(Iterable<ResultRecord> recordIterable, Iterable<ImmutableList<Object>> expectedData) {
        List<ImmutableList<Object>> remainingExpectedRows = new LinkedList<>();
        for (ImmutableList<Object> expectedRow : expectedData) {
            remainingExpectedRows.add(expectedRow);
        }
        for (ResultRecord record : recordIterable) {
            Iterator<ImmutableList<Object>> remainingIterator = remainingExpectedRows.iterator();
            boolean found = false;
            while (remainingIterator.hasNext()) {
                ImmutableList<Object> expectedRow = remainingIterator.next();
                if (recordMatcher.match(record, expectedRow)) {
                    remainingIterator.remove();
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remainingExpectedRows.isEmpty();
    }

}
