package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class ColumnHeaderNameMatcher implements ColumnHeaderMatcher {

    private final String expectedName;

    private ColumnHeaderNameMatcher(String expectedName) {
        this.expectedName = expectedName;
    }

    public static ColumnHeaderNameMatcher of(String expectedName) {
        return new ColumnHeaderNameMatcher(expectedName);
    }

    @Override
    public boolean match(MiniColumnHeader givenColumnHeader) {
        return givenColumnHeader.name().equals(expectedName);
    }

}
