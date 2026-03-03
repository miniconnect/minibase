package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.ResultRecord;

@FunctionalInterface
public interface KeyExtractor {

    public Object extract(ImmutableList<Object> row);

    public default Object extract(ResultRecord record) {
        return extract(record.getAll().map(ResultField::get));
    }

}
