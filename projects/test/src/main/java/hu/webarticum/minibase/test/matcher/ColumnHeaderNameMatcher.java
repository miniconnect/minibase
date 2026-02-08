package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;

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
