package hu.webarticum.minibase.storage.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.RangeSelection;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.api.TableSelection;
import hu.webarticum.minibase.storage.api.TableIndex.InclusionMode;
import hu.webarticum.minibase.storage.api.TableIndex.NullsMode;
import hu.webarticum.minibase.storage.api.TableIndex.SortMode;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable.SimpleTableBuilder;
import hu.webarticum.miniconnect.api.MiniErrorException;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;


public abstract class AbstractWritableTableTest {

    @Test
    protected void testWritable() {
        Table table = createSubjectTable();
        assertThat(table.isWritable()).isTrue();
    }

    @Test
    protected void testContent() {
        Table table = createSubjectTable();
        ImmutableList<ImmutableList<Object>> expectedContent = defaultContent();
        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testInsert() {
        Table table = createSubjectTable();
        TablePatch patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(15), "zzzz", 2))
                .insert(ImmutableList.of(LargeInteger.of(20), "yyyy", 3))
                .insert(ImmutableList.of(LargeInteger.of(25), "xxxx", 4))
                .build();
        table.applyPatch(patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                ImmutableList.of(LargeInteger.of(2), "bbbb", 1),
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jjjj", 5),
                ImmutableList.of(LargeInteger.of(8), "dddd", 2),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1),
                ImmutableList.of(LargeInteger.of(15), "zzzz", 2),
                ImmutableList.of(LargeInteger.of(20), "yyyy", 3),
                ImmutableList.of(LargeInteger.of(25), "xxxx", 4));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testUpdate() {
        Table table = createSubjectTable();
        TablePatch patch = TablePatch.builder()
                .update(LargeInteger.of(1L), ImmutableMap.of(1, "oooo"))
                .update(LargeInteger.of(2L), ImmutableMap.of(1, "pppp", 2, 0))
                .update(LargeInteger.of(5L), ImmutableMap.of(0, LargeInteger.of(106), 1, "qqqq"))
                .build();
        table.applyPatch(patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                ImmutableList.of(LargeInteger.of(2), "oooo", 1),
                ImmutableList.of(LargeInteger.of(3), "pppp", 0),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                ImmutableList.of(LargeInteger.of(106), "qqqq", 4),
                ImmutableList.of(LargeInteger.of(7), "jjjj", 5),
                ImmutableList.of(LargeInteger.of(8), "dddd", 2),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testDelete() {
        Table table = createSubjectTable();
        TablePatch patch = TablePatch.builder()
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(2))
                .delete(LargeInteger.of(3))
                .delete(LargeInteger.of(7))
                .build();
        table.applyPatch(patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(2), "bbbb", 1),
                ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jjjj", 5),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testComplex() {
        Table table = createSubjectTable();
        TablePatch patch = createDefaultComplexTablePatch();
        table.applyPatch(patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "ii", 0),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jj", 5),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1),
                ImmutableList.of(LargeInteger.of(101), "dd", 2),
                ImmutableList.of(LargeInteger.of(102), "ee", 4),
                ImmutableList.of(LargeInteger.of(103), "ff", 3),
                ImmutableList.of(LargeInteger.of(104), "gg", 5));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testComplexThenInsert() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch insertPatch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1001), "mmmmm", 1))
                .insert(ImmutableList.of(LargeInteger.of(1002), "nnnnn", 2))
                .build();
        table.applyPatch(insertPatch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "ii", 0),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jj", 5),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1),
                ImmutableList.of(LargeInteger.of(101), "dd", 2),
                ImmutableList.of(LargeInteger.of(102), "ee", 4),
                ImmutableList.of(LargeInteger.of(103), "ff", 3),
                ImmutableList.of(LargeInteger.of(104), "gg", 5),
                ImmutableList.of(LargeInteger.of(1001), "mmmmm", 1),
                ImmutableList.of(LargeInteger.of(1002), "nnnnn", 2));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testComplexThenUpdate() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch updatePatch = TablePatch.builder()
                .update(LargeInteger.ZERO, ImmutableMap.of(1, "111"))
                .update(LargeInteger.of(1), ImmutableMap.of(1, "222", 2, 5))
                .update(LargeInteger.of(4), ImmutableMap.of(1, "333"))
                .update(LargeInteger.of(5), ImmutableMap.of(1, "444"))
                .update(LargeInteger.of(6), ImmutableMap.of(1, "555"))
                .update(LargeInteger.of(8), ImmutableMap.of(1, "666"))
                .update(LargeInteger.of(10), ImmutableMap.of(0, LargeInteger.of(1104), 1, "777"))
                .build();
        table.applyPatch(updatePatch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "111", 2),
                ImmutableList.of(LargeInteger.of(3), "222", 5),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "ii", 0),
                ImmutableList.of(LargeInteger.of(6), "333", 4),
                ImmutableList.of(LargeInteger.of(7), "444", 5),
                ImmutableList.of(LargeInteger.of(10), "555", 1),
                ImmutableList.of(LargeInteger.of(101), "dd", 2),
                ImmutableList.of(LargeInteger.of(102), "666", 4),
                ImmutableList.of(LargeInteger.of(103), "ff", 3),
                ImmutableList.of(LargeInteger.of(1104), "777", 5));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testComplexThenDelete() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch deletePatch = TablePatch.builder()
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(1))
                .delete(LargeInteger.of(3))
                .delete(LargeInteger.of(7))
                .delete(LargeInteger.of(8))
                .build();
        table.applyPatch(deletePatch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jj", 5),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1),
                ImmutableList.of(LargeInteger.of(103), "ff", 3),
                ImmutableList.of(LargeInteger.of(104), "gg", 5));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testComplexThenComplex() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch complex2Patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1005), "YY", 1))
                .insert(ImmutableList.of(LargeInteger.of(1006), "ZZ", 2))
                .update(LargeInteger.of(4), ImmutableMap.of(1, "mmm", 2, 2))
                .update(LargeInteger.of(5), ImmutableMap.of(1, "nnn"))
                .update(LargeInteger.of(10), ImmutableMap.of(1, "ooo"))
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(2))
                .delete(LargeInteger.of(6))
                .delete(LargeInteger.of(8))
                .build();
        table.applyPatch(complex2Patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(5), "ii", 0),
                ImmutableList.of(LargeInteger.of(6), "mmm", 2),
                ImmutableList.of(LargeInteger.of(7), "nnn", 5),
                ImmutableList.of(LargeInteger.of(101), "dd", 2),
                ImmutableList.of(LargeInteger.of(103), "ff", 3),
                ImmutableList.of(LargeInteger.of(104), "ooo", 5),
                ImmutableList.of(LargeInteger.of(1005), "YY", 1),
                ImmutableList.of(LargeInteger.of(1006), "ZZ", 2));

        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }

    @Test
    protected void testThreeDeletes() {
        Table table = createSubjectTable();
        
        TablePatch delete1Patch = TablePatch.builder()
                .delete(LargeInteger.of(1))
                .delete(LargeInteger.of(6))
                .delete(LargeInteger.of(7))
                .build();
        table.applyPatch(delete1Patch);
        ImmutableList<ImmutableList<Object>> expectedContent1 = ImmutableList.of(
                        ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                        ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                        ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                        ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                        ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                        ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                        ImmutableList.of(LargeInteger.of(10), "ffff", 1));
        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent1.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent1);
        
        TablePatch delete2Patch = TablePatch.builder()
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(2))
                .build();
        table.applyPatch(delete2Patch);
        ImmutableList<ImmutableList<Object>> expectedContent2 = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1));
        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent2.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent2);
        
        TablePatch delete3Patch = TablePatch.builder()
                .delete(LargeInteger.of(1))
                .delete(LargeInteger.of(4))
                .build();
        table.applyPatch(delete3Patch);
        ImmutableList<ImmutableList<Object>> expectedContent3 = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3));
        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent3.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent3);
    }

    @Test
    protected void testIndexes() {
        Table table = createSubjectTable();
        NamedResourceStore<TableIndex> indexes = table.indexes();
        assertThat(indexes.names()).containsExactlyInAnyOrder(
                "idx_id", "idx_label", "idx_level");
        assertThat(indexes.get("idx_id").columnNames()).isEqualTo(
                ImmutableList.of("id"));
        assertThat(indexes.get("idx_label").columnNames()).isEqualTo(
                ImmutableList.of("label"));
        assertThat(indexes.get("idx_level").columnNames()).isEqualTo(
                ImmutableList.of("level"));
    }

    @Test
    protected void testIndexFind() {
        Table table = createSubjectTable();
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                "dddd",
                InclusionMode.EXCLUDE,
                "gggg",
                InclusionMode.INCLUDE,
                NullsMode.NO_NULLS,
                SortMode.UNSORTED);
        
        assertThat(selection).containsExactlyInAnyOrder(LargeInteger.arrayOf(0, 2, 9));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 2, 9));
    }

    @Test
    protected void testIndexFindAfterModifications() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch complex2Patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1005), "YY", 1))
                .insert(ImmutableList.of(LargeInteger.of(1006), "ZZ", 2))
                .update(LargeInteger.of(4), ImmutableMap.of(1, "mmm", 2, 2))
                .update(LargeInteger.of(5), ImmutableMap.of(1, "nnn"))
                .update(LargeInteger.of(10), ImmutableMap.of(1, "ooo"))
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(2))
                .delete(LargeInteger.of(6))
                .delete(LargeInteger.of(8))
                .build();
        table.applyPatch(complex2Patch);

        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                "gg",
                InclusionMode.INCLUDE,
                "yyy",
                InclusionMode.EXCLUDE,
                NullsMode.NO_NULLS,
                SortMode.UNSORTED);
        
        assertThat(selection).containsExactlyInAnyOrder(LargeInteger.arrayOf(0, 1, 2, 3, 6, 7));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 6, 7));
    }

    @Test
    protected void testIndexFindSortedAfterModifications() {
        Table table = createSubjectTable();
        TablePatch complexPatch = createDefaultComplexTablePatch();
        table.applyPatch(complexPatch);

        TablePatch complex2Patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1005), "KK", 2))
                .insert(ImmutableList.of(LargeInteger.of(1006), "ZZ", 4))
                .update(LargeInteger.of(4), ImmutableMap.of(1, "nnn", 2, 2))
                .update(LargeInteger.of(5), ImmutableMap.of(1, "mmm"))
                .update(LargeInteger.of(10), ImmutableMap.of(1, "ooo"))
                .delete(LargeInteger.ZERO)
                .delete(LargeInteger.of(2))
                .delete(LargeInteger.of(6))
                .delete(LargeInteger.of(8))
                .build();
        table.applyPatch(complex2Patch);

        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                "yyy",
                InclusionMode.EXCLUDE,
                "gg",
                InclusionMode.INCLUDE,
                NullsMode.NO_NULLS,
                SortMode.DESC_NULLS_LAST);

        assertThat(selection).containsExactly(LargeInteger.arrayOf(6, 2, 3, 7, 1, 0));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 6, 7));
    }

    @Test
    protected void testIndexFindExcludeNulls() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                "BBB",
                InclusionMode.EXCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.NO_NULLS,
                SortMode.ASC_NULLS_LAST);
        
        assertThatContainsUnstable(selection, new LargeInteger[][] {
            LargeInteger.arrayOf(7, 11),
            LargeInteger.arrayOf(0, 9),
        });
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 7, 9, 11));
    }

    @Test
    protected void testIndexFindIncludeNulls() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                null,
                InclusionMode.INCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.WITH_NULLS,
                SortMode.ASC_NULLS_FIRST);
        
        assertThatContainsUnstable(selection, new LargeInteger[][] {
            LargeInteger.arrayOf(1, 2, 4, 5, 8, 10),
            LargeInteger.arrayOf(3),
            LargeInteger.arrayOf(6),
            LargeInteger.arrayOf(7, 11),
            LargeInteger.arrayOf(0, 9),
        });
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    protected void testIndexFindNullsOnly() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                null,
                InclusionMode.INCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.NULLS_ONLY,
                SortMode.ASC_NULLS_FIRST);
        
        assertThat(selection).containsExactlyInAnyOrder((LargeInteger.arrayOf(1, 2, 4, 5, 8, 10)));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(1, 2, 4, 5, 8, 10));
    }

    @Test
    protected void testComplexWithNulls() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        TablePatch patch = createComplexTablePatchWithNulls();
        table.applyPatch(patch);

        ImmutableList<ImmutableList<Object>> expectedContent = ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), null, 2),
                ImmutableList.of(LargeInteger.of(2), null, 1),
                ImmutableList.of(LargeInteger.of(5), null, 5),
                ImmutableList.of(LargeInteger.of(6), null, 4),
                ImmutableList.of(LargeInteger.of(7), "BBB", 5),
                ImmutableList.of(LargeInteger.of(9), "NNN", 3),
                ImmutableList.of(LargeInteger.of(10), "DDD", 1),
                ImmutableList.of(LargeInteger.of(12), null, 1),
                ImmutableList.of(LargeInteger.of(101), "XXX", 2),
                ImmutableList.of(LargeInteger.of(102), null, 4),
                ImmutableList.of(LargeInteger.of(103), "YYY", 3),
                ImmutableList.of(LargeInteger.of(104), null, 5),
                ImmutableList.of(LargeInteger.of(105), "ZZZ", 2));
        
        assertThat(table.size()).isEqualTo(LargeInteger.of(expectedContent.size()));
        assertThat(contentOf(table)).isEqualTo(expectedContent);
    }
    
    @Test
    protected void testIndexFindExcludeNullsAfterModifications() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        
        TablePatch complexPatchWithNulls = createComplexTablePatchWithNulls();
        table.applyPatch(complexPatchWithNulls);
        
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                "BBB",
                InclusionMode.EXCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.NO_NULLS,
                SortMode.ASC_NULLS_LAST);

        assertThat(selection).containsExactly((LargeInteger.arrayOf(6, 5, 8, 10, 12)));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(5, 6, 8, 10, 12));
    }

    @Test
    protected void testIndexFindIncludeNullsAfterModifications() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        
        TablePatch complexPatchWithNulls = createComplexTablePatchWithNulls();
        table.applyPatch(complexPatchWithNulls);
        
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                null,
                InclusionMode.INCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.WITH_NULLS,
                SortMode.ASC_NULLS_FIRST);

        assertThatContainsUnstable(selection, new LargeInteger[][] {
            LargeInteger.arrayOf(0, 1, 2, 3, 7, 9, 11),
            LargeInteger.arrayOf(4),
            LargeInteger.arrayOf(6),
            LargeInteger.arrayOf(5),
            LargeInteger.arrayOf(8),
            LargeInteger.arrayOf(10),
            LargeInteger.arrayOf(12),
        });
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
    }

    @Test
    protected void testIndexFindNullsOnlyAfterModifications() {
        Table table = tableFrom(defaultColumnNames(), nullableColumnDefinitions(), contentWithNulls());
        
        TablePatch complexPatchWithNulls = createComplexTablePatchWithNulls();
        table.applyPatch(complexPatchWithNulls);
        
        TableIndex index = table.indexes().get("idx_label");
        TableSelection selection = index.find(
                null,
                InclusionMode.INCLUDE,
                null,
                InclusionMode.INCLUDE,
                NullsMode.NULLS_ONLY,
                SortMode.ASC_NULLS_FIRST);

        assertThat(selection).containsExactlyInAnyOrder(LargeInteger.arrayOf(0, 1, 2, 3, 7, 9, 11));
        assertThat(new RangeSelection(LargeInteger.ZERO, table.size()))
                .filteredOn(selection::containsRow)
                .containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 7, 9, 11));
    }

    @Test
    protected void testIllegalNullUpdate() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .update(LargeInteger.of(4), ImmutableMap.of(1, "ii", 2, 0))
                .update(LargeInteger.of(6), ImmutableMap.of(1, null, 2, 5))
                .build();
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    @Test
    protected void testIllegalNullInsert() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(104), null, 5))
                .insert(ImmutableList.of(LargeInteger.of(105), "ZZZ", 2))
                .build();
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    @Test
    protected void testIllegalNonUniqueUpdate() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .update(LargeInteger.of(4), ImmutableMap.of(0, LargeInteger.of(1), 1, "UUUU"))
                .build();
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    @Test
    protected void testIllegalNonUniqueInsert() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1), "UUUUU", 1))
                .build();
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    @Test
    protected void testIllegalNonUniqueDoubleInsert() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(1111), "UUUUU", 1))
                .build();
        
        table.applyPatch(patch); // apply in advance
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    @Test
    protected void testIllegalNonUniqueUpdateAndInsert() {
        Table table = createSubjectTable();
        
        TablePatch patch = TablePatch.builder()
                .update(LargeInteger.of(4), ImmutableMap.of(0, LargeInteger.of(1111), 1, "UUUU"))
                .insert(ImmutableList.of(LargeInteger.of(1111), "UUUUU", 1))
                .build();
        
        assertThatThrownBy(() -> table.applyPatch(patch)).isInstanceOf(MiniErrorException.class);
    }

    
    protected Table createSubjectTable() {
        return tableFrom(defaultColumnNames(), defaultColumnDefinitions(), defaultContent());
    }

    protected SimpleTable createSimpleTable() {
        return simpleTableFrom(defaultColumnNames(), defaultColumnDefinitions(), defaultContent());
    }
    
    protected ImmutableList<String> defaultColumnNames() {
        return ImmutableList.of("id", "label", "level");
    }

    protected ImmutableList<ColumnDefinition> defaultColumnDefinitions() {
        return ImmutableList.of(
                new SimpleColumnDefinition(LargeInteger.class, false, true),
                new SimpleColumnDefinition(String.class, false),
                new SimpleColumnDefinition(Integer.class, false));
    }

    protected ImmutableList<ColumnDefinition> nullableColumnDefinitions() {
        return ImmutableList.of(
                new SimpleColumnDefinition(LargeInteger.class, false),
                new SimpleColumnDefinition(String.class, true),
                new SimpleColumnDefinition(Integer.class, true));
    }

    protected ImmutableList<ImmutableList<Object>> defaultContent() {
        return ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "eeee", 2),
                ImmutableList.of(LargeInteger.of(2), "bbbb", 1),
                ImmutableList.of(LargeInteger.of(3), "gggg", 3),
                ImmutableList.of(LargeInteger.of(4), "cccc", 4),
                ImmutableList.of(LargeInteger.of(5), "aaaa", 5),
                ImmutableList.of(LargeInteger.of(6), "hhhh", 4),
                ImmutableList.of(LargeInteger.of(7), "jjjj", 5),
                ImmutableList.of(LargeInteger.of(8), "dddd", 2),
                ImmutableList.of(LargeInteger.of(9), "iiii", 3),
                ImmutableList.of(LargeInteger.of(10), "ffff", 1));
    }

    protected ImmutableList<ImmutableList<Object>> contentWithNulls() {
        return ImmutableList.of(
                ImmutableList.of(LargeInteger.of(1), "DDD", 1),
                ImmutableList.of(LargeInteger.of(2), null, 1),
                ImmutableList.of(LargeInteger.of(3), null, 3),
                ImmutableList.of(LargeInteger.of(4), "AAA", 3),
                ImmutableList.of(LargeInteger.of(5), null, 5),
                ImmutableList.of(LargeInteger.of(6), null, 4),
                ImmutableList.of(LargeInteger.of(7), "BBB", 5),
                ImmutableList.of(LargeInteger.of(8), "CCC", 2),
                ImmutableList.of(LargeInteger.of(9), null, 3),
                ImmutableList.of(LargeInteger.of(10), "DDD", 1),
                ImmutableList.of(LargeInteger.of(11), null, 1),
                ImmutableList.of(LargeInteger.of(12), "CCC", 1));
    }
    
    protected ImmutableList<ImmutableList<Object>> contentOf(Table table) {
        List<ImmutableList<Object>> resultBuilder = new ArrayList<>();
        LargeInteger size = table.size();
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(LargeInteger.ONE)) {
            resultBuilder.add(table.row(i).getAll());
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    protected abstract Table tableFrom(
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableList<ImmutableList<Object>> content);
    
    protected SimpleTable simpleTableFrom(
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableList<ImmutableList<Object>> content) {
        SimpleTableBuilder builder = SimpleTable.builder();
        columnNames.forEachIndex((i, n) -> builder.addColumn(n, columnDefinitions.get(i)));
        content.forEach(builder::addRow);
        columnNames.forEach(n -> builder.addIndex("idx_" + n, ImmutableList.of(n)));
        return builder.build();
    }
    
    protected TablePatch createDefaultComplexTablePatch() {
        return TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(101), "dd", 2))
                .insert(ImmutableList.of(LargeInteger.of(102), "ee", 4))
                .insert(ImmutableList.of(LargeInteger.of(103), "ff", 3))
                .insert(ImmutableList.of(LargeInteger.of(104), "gg", 5))
                .update(LargeInteger.of(4), ImmutableMap.of(1, "ii", 2, 0))
                .update(LargeInteger.of(6), ImmutableMap.of(1, "jj", 2, 5))
                .delete(LargeInteger.of(1))
                .delete(LargeInteger.of(7))
                .delete(LargeInteger.of(8))
                .build();
    }

    protected TablePatch createComplexTablePatchWithNulls() {
        return TablePatch.builder()
                .insert(ImmutableList.of(LargeInteger.of(101), "XXX", 2))
                .insert(ImmutableList.of(LargeInteger.of(102), null, 4))
                .insert(ImmutableList.of(LargeInteger.of(103), "YYY", 3))
                .insert(ImmutableList.of(LargeInteger.of(104), null, 5))
                .insert(ImmutableList.of(LargeInteger.of(105), "ZZZ", 2))
                .update(LargeInteger.ZERO, ImmutableMap.of(1, null, 2, 2))
                .update(LargeInteger.of(8), ImmutableMap.of(1, "NNN"))
                .update(LargeInteger.of(11), ImmutableMap.of(1, null))
                .delete(LargeInteger.of(2))
                .delete(LargeInteger.of(3))
                .delete(LargeInteger.of(7))
                .delete(LargeInteger.of(10))
                .build();
    }
    
    protected void assertThatContainsUnstable(Iterable<LargeInteger> selection, LargeInteger[][] equalGroups) {
        Iterator<LargeInteger> iterator = selection.iterator();
        for (LargeInteger[] equalGroup : equalGroups) {
            int groupSize = equalGroup.length;
            List<LargeInteger> foundValues = new ArrayList<>(groupSize);
            for (int i = 0; i < groupSize; i++) {
                assertThat(iterator).hasNext();
                foundValues.add(iterator.next());
            }
            assertThat(foundValues).containsExactlyInAnyOrder(equalGroup);
        }
        assertThat(iterator).isExhausted();
    }
    
}
