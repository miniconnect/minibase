package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class ColumnHeaderMatcherList implements ColumnHeaderMatcher {

    private final ImmutableList<ColumnHeaderMatcher> matchers;

    private ColumnHeaderMatcherList(ImmutableList<ColumnHeaderMatcher> matchers) {
        this.matchers = matchers;
    }

    public static ColumnHeaderMatcherList of(ImmutableList<ColumnHeaderMatcher> matchers) {
        return new ColumnHeaderMatcherList(matchers);
    }

    @Override
    public boolean match(MiniColumnHeader givenColumnHeader) {
        for (ColumnHeaderMatcher matcher : matchers) {
            if (!matcher.match(givenColumnHeader)) {
                return false;
            }
        }
        return true;
    }

}
