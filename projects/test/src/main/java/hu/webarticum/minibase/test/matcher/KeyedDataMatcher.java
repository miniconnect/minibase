package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.HashMap;
import java.util.Map;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class KeyedDataMatcher implements DataMatcher {

    private final RecordMatcher recordMatcher;

    private final KeyExtractor keyExtractor;

    private KeyedDataMatcher(RecordMatcher recordMatcher, KeyExtractor keyExtractor) {
        this.recordMatcher = recordMatcher;
        this.keyExtractor = keyExtractor;
    }

    public static KeyedDataMatcher of(RecordMatcher recordMatcher, KeyExtractor keyExtractor) {
        return new KeyedDataMatcher(recordMatcher, keyExtractor);
    }

    @Override
    public void match(Iterable<ResultRecord> givenRecords, Iterable<ImmutableList<Object>> expectedData) throws Exception {
        Map<Object, ImmutableList<Object>> expectedDataMap = new HashMap<>();
        for (ImmutableList<Object> expectedRow : expectedData) {
            Object key = keyExtractor.extract(expectedRow);
            if (expectedDataMap.containsKey(key)) {
                throw new MatchFailedException("duplicated expected record key: " + key);
            }
            expectedDataMap.put(key, expectedRow);
        }
        for (ResultRecord record : givenRecords) {
            Object key = keyExtractor.extract(record);
            if (!expectedDataMap.containsKey(key)) {
                throw new MatchFailedException("unexpected record key: " + key);
            }
            ImmutableList<Object> expectedRow = expectedDataMap.remove(key);
            try {
                recordMatcher.match(record, expectedRow);
            } catch (Exception e) {
                throw MatchFailedException.prefix("at key " + key + ": ", e);
            }
        }
        if (!expectedDataMap.isEmpty()) {
            throw new MatchFailedException("too few records (" + expectedDataMap.size() + " keys are missing) " + expectedDataMap);
        }
    }

}
