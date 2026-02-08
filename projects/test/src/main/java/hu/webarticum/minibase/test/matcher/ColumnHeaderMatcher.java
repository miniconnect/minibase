package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;

@FunctionalInterface
public interface ColumnHeaderMatcher {

    public boolean match(MiniColumnHeader givenColumnHeader);

}
