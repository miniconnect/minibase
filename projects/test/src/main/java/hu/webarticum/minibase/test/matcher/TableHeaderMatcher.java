package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

@FunctionalInterface
public interface TableHeaderMatcher {

    public boolean match(ImmutableList<MiniColumnHeader> columnHeaders);

}
