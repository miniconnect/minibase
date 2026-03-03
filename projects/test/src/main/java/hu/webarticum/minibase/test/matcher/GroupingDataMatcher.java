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

    private GroupingDataMatcher(DataMatcher groupDataMatcher, KeyExtractor groupingKeyExtractor) {
        this.groupDataMatcher = groupDataMatcher;
        this.groupingKeyExtractor = groupingKeyExtractor;
    }

    public static GroupingDataMatcher of(DataMatcher groupDataMatcher, KeyExtractor groupingKeyExtractor) {
        return new GroupingDataMatcher(groupDataMatcher, groupingKeyExtractor);
    }

    @Override
    public void match(Iterable<ResultRecord> givenRecords, Iterable<ImmutableList<Object>> expectedData) throws Exception {
        Iterator<ImmutableList<Object>> expectedDataIterator = expectedData.iterator();
        List<ResultRecord> currentGroupGivenRecords = new ArrayList<>();
        List<ImmutableList<Object>> currentGroupExpectedRows = new ArrayList<>();
        Object currentGroupKey = null;
        int i = 0;
        for (ResultRecord record : givenRecords) {
            if (!expectedDataIterator.hasNext()) {
                throw new MatchFailedException("too many records");
            }
            Object foundGroupKey = groupingKeyExtractor.extract(record);
            ImmutableList<Object> expectedRow = expectedDataIterator.next();
            Object expectedGroupKey = groupingKeyExtractor.extract(expectedRow);
            if (!Objects.equals(foundGroupKey, expectedGroupKey)) {
                throw new MatchFailedException("at row " + i + ": group key: " + foundGroupKey + " != " + expectedGroupKey);
            }
            if (!Objects.equals(foundGroupKey, currentGroupKey)) {
                try {
                    groupDataMatcher.match(currentGroupGivenRecords, currentGroupExpectedRows);
                } catch (Exception e) {
                    throw MatchFailedException.prefix("at group key " + currentGroupKey + ": ", e);
                }
                currentGroupGivenRecords.clear();
                currentGroupExpectedRows.clear();
                currentGroupKey = foundGroupKey;
            }
            currentGroupGivenRecords.add(record);
            currentGroupExpectedRows.add(expectedRow);
            i++;
        }
        if (expectedDataIterator.hasNext()) {
            throw new MatchFailedException("too few records");
        }
        try {
            groupDataMatcher.match(currentGroupGivenRecords, currentGroupExpectedRows);
        } catch (Exception e) {
            throw MatchFailedException.prefix("at group key " + currentGroupKey + ": ", e);
        }
    }

}
