package hu.webarticum.minibase.test.model.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.minibase.test.model.AbstractResourceBasedTest;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

class DatasetDescriptionTest extends AbstractResourceBasedTest {

    private final static String DATASET_RESOURCE = "hu/webarticum/minibase/test/sample/dataset.yaml";

    @Test
    void testMapping() throws IOException {
        DatasetDescription datasetDescription = loadDatasetDescription();
        assertThat(datasetDescription.schemas()).hasSize(1);
        DatasetSchemaDescription schema = datasetDescription.schemas().get(0);
        ImmutableList<DatasetTableDescription> tables = schema.tables();
        assertThat(tables).hasSize(2);
        testTable1(tables.get(0));
        testTable2(tables.get(1));
    }

    void testTable1(DatasetTableDescription table) {
        assertThat(table.name()).isEqualTo("tbl_1");
        assertThat(table.addDiffLayer()).isFalse();
        ImmutableList<DatasetColumnDescription> columns = table.columns();
        assertThat(columns).hasSize(2);
        DatasetColumnDescription idColumn = columns.get(0);
        assertThat(idColumn.name()).isEqualTo("id");
        assertThat(idColumn.type()).isEqualTo(LargeInteger.class);
        assertThat(idColumn.nullable()).isFalse();
        assertThat(idColumn.unique()).isTrue();
        assertThat(idColumn.autoIncremented()).isTrue();
        assertThat(idColumn.enumValues()).isEmpty();
        assertThat(idColumn.defaultValue()).isEmpty();
        DatasetColumnDescription labelColumn = columns.get(1);
        assertThat(labelColumn.name()).isEqualTo("label");
        assertThat(labelColumn.type()).isEqualTo(String.class);
        assertThat(labelColumn.nullable()).isFalse();
        assertThat(labelColumn.unique()).isFalse();
        assertThat(labelColumn.autoIncremented()).isFalse();
        assertThat(labelColumn.enumValues()).isEmpty();
        assertThat(labelColumn.defaultValue()).contains("");
        assertThat(table.indexes()).isEmpty();
        assertThat(table.data()).containsExactly(ImmutableList.of(1, "xyz"));
    }

    void testTable2(DatasetTableDescription table) {
        assertThat(table.name()).isEqualTo("tbl_2");
        assertThat(table.addDiffLayer()).isTrue();
        ImmutableList<DatasetColumnDescription> columns = table.columns();
        assertThat(columns).hasSize(4);
        DatasetColumnDescription idColumn = columns.get(0);
        assertThat(idColumn.name()).isEqualTo("id");
        assertThat(idColumn.type()).isEqualTo(LargeInteger.class);
        assertThat(idColumn.nullable()).isFalse();
        assertThat(idColumn.unique()).isTrue();
        assertThat(idColumn.autoIncremented()).isTrue();
        assertThat(idColumn.enumValues()).isEmpty();
        assertThat(idColumn.defaultValue()).isEmpty();
        DatasetColumnDescription nameColumn = columns.get(1);
        assertThat(nameColumn.name()).isEqualTo("name");
        assertThat(nameColumn.type()).isEqualTo(String.class);
        assertThat(nameColumn.nullable()).isFalse();
        assertThat(nameColumn.unique()).isFalse();
        assertThat(nameColumn.autoIncremented()).isFalse();
        assertThat(nameColumn.enumValues()).isEmpty();
        assertThat(nameColumn.defaultValue()).contains("hello");
        DatasetColumnDescription typeColumn = columns.get(2);
        assertThat(typeColumn.name()).isEqualTo("type");
        assertThat(typeColumn.type()).isEqualTo(String.class);
        assertThat(typeColumn.nullable()).isFalse();
        assertThat(typeColumn.unique()).isFalse();
        assertThat(typeColumn.autoIncremented()).isFalse();
        assertThat(typeColumn.enumValues()).contains(ImmutableList.of("APPLE", "ORANGE"));
        assertThat(typeColumn.defaultValue()).isEmpty();
        DatasetColumnDescription descriptionColumn = columns.get(3);
        assertThat(descriptionColumn.name()).isEqualTo("description");
        assertThat(descriptionColumn.type()).isEqualTo(String.class);
        assertThat(descriptionColumn.nullable()).isTrue();
        assertThat(descriptionColumn.unique()).isFalse();
        assertThat(descriptionColumn.autoIncremented()).isFalse();
        assertThat(descriptionColumn.enumValues()).isEmpty();
        assertThat(descriptionColumn.defaultValue()).isEmpty();
        assertThat(table.indexes()).containsExactly(ImmutableList.of("id"));
        assertThat(table.data()).containsExactly(
                ImmutableList.of(1, "lorem", "APPLE", "Some description"),
                ImmutableList.of(2, "ipsum", "ORANGE", null));
    }

    private DatasetDescription loadDatasetDescription() throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(DATASET_RESOURCE, "dataset resource stream")) {
            return mapper.readValue(in, DatasetDescription.class);
        }
    }

}
