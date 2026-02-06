package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;

@FunctionalInterface
public interface ColumnHeaderMatcher {

    public boolean match(MiniColumnHeader columnHeader);

}
