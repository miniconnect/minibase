package hu.webarticum.minibase.test.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class DefaultTableHeaderMatcher implements TableHeaderMatcher {

    private final ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers;

    private DefaultTableHeaderMatcher(ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers) {
        this.columnHeaderMatchers = columnHeaderMatchers;
    }

    public static DefaultTableHeaderMatcher of(ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers) {
        return new DefaultTableHeaderMatcher(columnHeaderMatchers);
    }

    @Override
    public boolean match(ImmutableList<MiniColumnHeader> columnHeaders) {
        int tableWidth = columnHeaderMatchers.size();
        if (columnHeaders.size() != tableWidth) {
            return false;
        }
        for (int i = 0; i < tableWidth; i++) {
            if (!columnHeaderMatchers.get(i).match(columnHeaders.get(i))) {
                return false;
            }
        }
        return true;
    }

}
