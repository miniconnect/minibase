package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

@FunctionalInterface
public interface ColumnHeaderMatcher {

    public boolean match(MiniColumnHeader columnHeader);

}
