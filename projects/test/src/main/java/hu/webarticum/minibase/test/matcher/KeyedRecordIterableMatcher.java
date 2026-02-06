package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.HashMap;
import java.util.Map;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.ResultRecord;

public class KeyedRecordIterableMatcher implements RecordIterableMatcher {

    private final RecordMatcher recordMatcher;

    private final KeyExtractor keyExtractor;

    public KeyedRecordIterableMatcher(RecordMatcher recordMatcher, KeyExtractor keyExtractor) {
        this.recordMatcher = recordMatcher;
        this.keyExtractor = keyExtractor;
    }

    public boolean match(Iterable<ResultRecord> recordIterable, ImmutableList<ImmutableList<Object>> expectedData) {
        Map<Object, ResultRecord> recordMap = new HashMap<>();
        for (ResultRecord record : recordIterable) {
            Object key = keyExtractor.extract(record);
            if (recordMap.containsKey(key)) {
                return false;
            }
            recordMap.put(key, record);
        }
        Map<Object, ImmutableList<Object>> expectedDataMap = new HashMap<>();
        for (ImmutableList<Object> expectedValues : expectedData) {
            Object key = keyExtractor.extract(expectedValues);
            if (expectedDataMap.containsKey(key)) {
                return false;
            }
            expectedDataMap.put(key, expectedValues);
        }
        if (!recordMap.keySet().equals(expectedDataMap.keySet())) {
            return false;
        }
        for (Map.Entry<Object, ResultRecord> entry : recordMap.entrySet()) {
            Object key = entry.getKey();
            ResultRecord record = entry.getValue();
            ImmutableList<Object> expectedValues = expectedDataMap.get(key);
            if (!recordMatcher.match(record, expectedValues)) {
                return false;
            }
        }
        return true;
    }

}
