package hu.webarticum.minibase.test.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.minibase.storage.impl.diff.DiffTable;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable;
import hu.webarticum.minibase.test.model.dataset.DatasetColumnDescription;
import hu.webarticum.minibase.test.model.dataset.DatasetTableDescription;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class TableRendererTest {

    private TableRenderer tableRenderer;

    @Test
    void testSimple() {
        DatasetTableDescription tableDescription = new DatasetTableDescription(
                "some_table",
                false,
                ImmutableList.of(
                        new DatasetColumnDescription("id", LargeInteger.class, false, true, true, null, null),
                        new DatasetColumnDescription("label", String.class, true, true, false, null, null)),
                ImmutableList.of(ImmutableList.of("id")),
                ImmutableList.of(
                        ImmutableList.of(LargeInteger.ONE, "lorem"),
                        ImmutableList.of(LargeInteger.TWO, "ipsum")));
        Table table = tableRenderer.renderTable(tableDescription);
        assertThat(table).isInstanceOf(SimpleTable.class);
        assertThat(table.name()).isEqualTo("some_table");

        NamedResourceStore<Column> columns = table.columns();
        assertThat(columns.names()).containsExactly("id", "label");

        Column idColumn = columns.get("id");
        assertThat(idColumn.name()).isEqualTo("id");
        ColumnDefinition idDefinition = idColumn.definition();
        assertThat(idDefinition.clazz()).isEqualTo(LargeInteger.class);
        assertThat(idDefinition.isNullable()).isFalse();
        assertThat(idDefinition.isUnique()).isTrue();
        assertThat(idDefinition.isAutoIncremented()).isTrue();
        assertThat(idDefinition.enumValues()).isEmpty();
        @SuppressWarnings("unchecked")
        Comparator<LargeInteger> idComparator = (Comparator<LargeInteger>) idDefinition.comparator();
        assertThat(idComparator.compare(LargeInteger.TWO, LargeInteger.ONE)).isPositive();
        assertThat(idDefinition.defaultValue()).isNull();

        Column labelColumn = columns.get("label");
        assertThat(labelColumn.name()).isEqualTo("label");
        ColumnDefinition labelDefinition = labelColumn.definition();
        assertThat(labelDefinition.clazz()).isEqualTo(String.class);
        assertThat(labelDefinition.isNullable()).isTrue();
        assertThat(labelDefinition.isUnique()).isTrue();
        assertThat(labelDefinition.isAutoIncremented()).isFalse();
        assertThat(labelDefinition.enumValues()).isEmpty();
        @SuppressWarnings("unchecked")
        Comparator<String> labelComparator = (Comparator<String>) labelDefinition.comparator();
        assertThat(labelComparator.compare("lorem", "ipsum")).isPositive();
        assertThat(labelDefinition.defaultValue()).isNull();

        NamedResourceStore<TableIndex> indexes = table.indexes();
        assertThat(indexes.names()).containsExactly("idx_id");
        assertThat(indexes.get("idx_id").columnNames()).containsExactly("id");

        assertThat(table.size()).isEqualTo(2);
        assertThat(table.row(LargeInteger.ZERO).getAll()).containsExactly(LargeInteger.ONE, "lorem");
        assertThat(table.row(LargeInteger.ONE).getAll()).containsExactly(LargeInteger.TWO, "ipsum");
    }

    @Test
    void testWithDiffLayer() {
        DatasetTableDescription tableDescription = new DatasetTableDescription(
                "diff_table",
                true,
                ImmutableList.of(
                        new DatasetColumnDescription("id", LargeInteger.class, false, true, true, null, null)),
                ImmutableList.of(ImmutableList.of("id")),
                ImmutableList.of(
                        ImmutableList.of(LargeInteger.ONE)));
        Table table = tableRenderer.renderTable(tableDescription);
        assertThat(table).isInstanceOf(DiffTable.class);
        assertThat(table.name()).isEqualTo("diff_table");

        NamedResourceStore<Column> columns = table.columns();
        assertThat(columns.names()).containsExactly("id");

        Column idColumn = columns.get("id");
        assertThat(idColumn.name()).isEqualTo("id");
        ColumnDefinition idDefinition = idColumn.definition();
        assertThat(idDefinition.clazz()).isEqualTo(LargeInteger.class);
        assertThat(idDefinition.isNullable()).isFalse();
        assertThat(idDefinition.isUnique()).isTrue();
        assertThat(idDefinition.isAutoIncremented()).isTrue();
        assertThat(idDefinition.enumValues()).isEmpty();
        @SuppressWarnings("unchecked")
        Comparator<LargeInteger> idComparator = (Comparator<LargeInteger>) idDefinition.comparator();
        assertThat(idComparator.compare(LargeInteger.TWO, LargeInteger.ONE)).isPositive();
        assertThat(idDefinition.defaultValue()).isNull();

        NamedResourceStore<TableIndex> indexes = table.indexes();
        assertThat(indexes.names()).containsExactly("idx_id");
        assertThat(indexes.get("idx_id").columnNames()).containsExactly("id");

        assertThat(table.size()).isEqualTo(1);
        assertThat(table.row(LargeInteger.ZERO).getAll()).containsExactly(LargeInteger.ONE);
    }

    @BeforeEach
    public void init() {
        tableRenderer = new TableRenderer();
    }

}
