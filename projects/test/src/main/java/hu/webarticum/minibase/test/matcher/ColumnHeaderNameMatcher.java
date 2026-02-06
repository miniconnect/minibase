package hu.webarticum.minibase.test.matcher;

import java.lang.Iterable;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class ColumnHeaderNameMatcher implements ColumnHeaderMatcher {

    private final String expectedName;

    public ColumnHeaderNameMatcher(String expectedName) {
        this.expectedName = expectedName;
    }

    @Override
    public boolean match(MiniColumnHeader columnHeader) {
        return columnHeader.name().equals(expectedName);
    }

}
