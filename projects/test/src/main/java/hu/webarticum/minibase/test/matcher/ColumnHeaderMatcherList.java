package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class ColumnHeaderMatcherList implements ColumnHeaderMatcher {

    private final ImmutableList<ColumnHeaderMatcher> matchers;

    public ColumnHeaderMatcherList(ImmutableList<ColumnHeaderMatcher> matchers) {
        this.matchers = matchers;
    }

    @Override
    public boolean match(MiniColumnHeader columnHeader) {
        for (ColumnHeaderMatcher matcher : matchers) {
            if (!matcher.match(columnHeader)) {
                return false;
            }
        }
        return true;
    }

}
