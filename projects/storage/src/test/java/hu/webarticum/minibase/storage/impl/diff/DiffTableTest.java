package hu.webarticum.minibase.storage.impl.diff;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.impl.AbstractWritableTableTest;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

class DiffTableTest extends AbstractWritableTableTest {

    @Test
    void testBaseTableUntouched() {
        Table baseTable = createSimpleTable();
        DiffTable diffTable = new DiffTable(baseTable);
        
        TablePatch patch1 = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(11), "AAA", true))
                .insert(ImmutableList.of(LargeInteger.of(12), "BBB", false))
                .insert(ImmutableList.of(LargeInteger.of(13), "CCC", true))
                .update(LargeInteger.of(2), ImmutableMap.of(1, "UUU"))
                .update(LargeInteger.of(3), ImmutableMap.of(1, "VVV"))
                .delete(LargeInteger.ZERO)
                .build();
        diffTable.applyPatch(patch1);

        TablePatch patch2 = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(14), "XXX", true))
                .update(LargeInteger.of(1), ImmutableMap.of(1, "uuuu", 2, false))
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(3))
                .build();
        diffTable.applyPatch(patch2);

        assertThat(contentOf(baseTable)).isEqualTo(defaultContent());
    }

    @Override
    protected DiffTable tableFrom(
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableList<ImmutableList<Object>> content) {
        return new DiffTable(simpleTableFrom(columnNames, columnDefinitions, content));
    }
    
}
