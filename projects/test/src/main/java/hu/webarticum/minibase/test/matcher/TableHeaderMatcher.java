package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

@FunctionalInterface
public interface TableHeaderMatcher {

    public void match(ImmutableList<MiniColumnHeader> givenColumnHeaders) throws Exception;

}
