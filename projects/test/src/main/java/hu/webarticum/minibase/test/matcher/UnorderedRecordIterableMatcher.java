package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class UnorderedRecordIterableMatcher implements RecordIterableMatcher {

    private final RecordMatcher recordMatcher;

    public UnorderedRecordIterableMatcher(RecordMatcher recordMatcher) {
        this.recordMatcher = recordMatcher;
    }

    public boolean match(Iterable<ResultRecord> recordIterable, ImmutableList<ImmutableList<Object>> expectedData) {
        List<ImmutableList<Object>> remainingExpectedRows = new LinkedList<>(expectedData.asList());
        for (ResultRecord record : recordIterable) {
            Iterator <ImmutableList<Object>> remainingIterator = remainingExpectedRows.iterator();
            boolean found = false;
            while (remainingIterator.hasNext()) {
                ImmutableList<Object> expectedValues = remainingIterator.next();
                if (recordMatcher.match(record, expectedValues)) {
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
