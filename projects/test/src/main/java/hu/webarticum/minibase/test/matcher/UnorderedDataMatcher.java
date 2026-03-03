package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class UnorderedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    private UnorderedDataMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    public static UnorderedDataMatcher of(RecordMatcher recordMatcher) {
        return new UnorderedDataMatcher(recordMatcher);
    }

    @Override
    public void match(Iterable<ResultRecord> givenRecords, Iterable<ImmutableList<Object>> expectedData) throws Exception {
        List<ImmutableList<Object>> remainingExpectedRows = new LinkedList<>();
        for (ImmutableList<Object> expectedRow : expectedData) {
            remainingExpectedRows.add(expectedRow);
        }
        for (ResultRecord record : givenRecords) {
            Iterator<ImmutableList<Object>> remainingIterator = remainingExpectedRows.iterator();
            boolean found = false;
            while (remainingIterator.hasNext()) {
                ImmutableList<Object> expectedRow = remainingIterator.next();
                if (recordMatcher.isMatching(record, expectedRow)) {
                    remainingIterator.remove();
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new MatchFailedException("row not found: " + record.getAll());
            }
        }
        if (!remainingExpectedRows.isEmpty()) {
            throw new MatchFailedException("too few rows");
        }
    }

}
