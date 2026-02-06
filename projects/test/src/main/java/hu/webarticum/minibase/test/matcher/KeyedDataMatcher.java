package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.HashMap;
import java.util.Map;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.ResultRecord;

public class KeyedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    private final KeyExtractor keyExtractor;

    public KeyedDataMatcher(RecordMatcher recordMatcher, KeyExtractor keyExtractor) {
        this.recordMatcher = recordMatcher;
        this.keyExtractor = keyExtractor;
    }

    @Override
    public boolean match(Iterable<ResultRecord> recordIterable, Iterable<ImmutableList<Object>> expectedData) {
        Map<Object, ImmutableList<Object>> expectedDataMap = new HashMap<>();
        for (ImmutableList<Object> expectedRow : expectedData) {
            Object key = keyExtractor.extract(expectedRow);
            if (expectedDataMap.containsKey(key)) {
                return false;
            }
            expectedDataMap.put(key, expectedRow);
        }
        for (ResultRecord record : recordIterable) {
            Object key = keyExtractor.extract(record);
            if (!expectedDataMap.containsKey(key)) {
                return false;
            }
            ImmutableList<Object> expectedRow = expectedDataMap.remove(key);
            if (!recordMatcher.match(record, expectedRow)) {
                return false;
            }
        }
        return expectedDataMap.isEmpty();
    }

}
