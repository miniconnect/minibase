package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultRecord;

public class GroupingDataMatcher implements DataMatcher {

    private final DataMatcher groupDataMatcher;

    private final KeyExtractor groupingKeyExtractor;

    public GroupingDataMatcher(DataMatcher groupDataMatcher, KeyExtractor groupingKeyExtractor) {
        this.groupDataMatcher = groupDataMatcher;
        this.groupingKeyExtractor = groupingKeyExtractor;
    }

    public boolean match(Iterable<ResultRecord> recordIterable, Iterable<ImmutableList<Object>> expectedData) {
        Iterator<ImmutableList<Object>> expectedDataIterator = expectedData.iterator();
        List<ResultRecord> currentGroupGivenRecords = new ArrayList<>();
        List<ImmutableList<Object>> currentGroupExpectedRows = new ArrayList<>();
        Object currentGroupKey = null;
        for (ResultRecord record : recordIterable) {
            if (!expectedDataIterator.hasNext()) {
                return false;
            }
            Object foundGroupKey = groupingKeyExtractor.extract(record);
            ImmutableList<Object> expectedRow = expectedDataIterator.next();
            Object expectedGroupKey = groupingKeyExtractor.extract(expectedRow);
            if (!Objects.equals(foundGroupKey, expectedGroupKey)) {
                return false;
            }
            if (!Objects.equals(foundGroupKey, currentGroupKey)) {
                if (!groupDataMatcher.match(currentGroupGivenRecords, currentGroupExpectedRows)) {
                    return false;
                }
                currentGroupGivenRecords.clear();
                currentGroupExpectedRows.clear();
                currentGroupKey = foundGroupKey;
            }
            currentGroupGivenRecords.add(record);
            currentGroupExpectedRows.add(expectedRow);
        }
        if (expectedDataIterator.hasNext()) {
            return false;
        }
        if (!groupDataMatcher.match(currentGroupGivenRecords, currentGroupExpectedRows)) {
            return false;
        }
        return false;
    }

}
