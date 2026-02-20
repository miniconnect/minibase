package hu.webarticum.minibase.test.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.minibase.engine.impl.SimpleEngine;
import hu.webarticum.minibase.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.minibase.query.parser.AntlrSqlParser;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.impl.simple.SimpleResourceManager;
import hu.webarticum.minibase.storage.impl.simple.SimpleSchema;
import hu.webarticum.minibase.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.minibase.test.matcher.ColumnHeaderMatcher;
import hu.webarticum.minibase.test.matcher.DataMatcher;
import hu.webarticum.minibase.test.matcher.DefaultRecordMatcher;
import hu.webarticum.minibase.test.matcher.DefaultTableHeaderMatcher;
import hu.webarticum.minibase.test.matcher.DefaultTableMatcher;
import hu.webarticum.minibase.test.matcher.EqualityFieldMatcher;
import hu.webarticum.minibase.test.matcher.GroupingDataMatcher;
import hu.webarticum.minibase.test.matcher.KeyedDataMatcher;
import hu.webarticum.minibase.test.matcher.MatchFailedException;
import hu.webarticum.minibase.test.matcher.OrderedDataMatcher;
import hu.webarticum.minibase.test.matcher.RecordMatcher;
import hu.webarticum.minibase.test.matcher.TableHeaderMatcher;
import hu.webarticum.minibase.test.matcher.TableMatcher;
import hu.webarticum.minibase.test.matcher.UnorderedDataMatcher;
import hu.webarticum.minibase.test.model.dataset.DatasetDescription;
import hu.webarticum.minibase.test.model.dataset.DatasetSchemaDescription;
import hu.webarticum.minibase.test.model.dataset.DatasetTableDescription;
import hu.webarticum.minibase.test.model.suite.QueryTestCaseDescription;
import hu.webarticum.minibase.test.model.suite.QueryTestResultColumnDescription;
import hu.webarticum.minibase.test.model.suite.QueryTestSuiteDescription;
import hu.webarticum.minibase.test.model.suite.QueryTestSuiteListDescription;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;
import hu.webarticum.miniconnect.record.ResultTable;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.record.type.StandardValueType;

public class QueryTestController {

    private final TableRenderer tableRenderer = new TableRenderer();

    private final Converter converter = new DefaultConverter();

    private final String suiteListResourcePath;

    private QueryTestController(String suiteListResourcePath) {
        this.suiteListResourcePath = suiteListResourcePath;
    }

    public static QueryTestController ofResource(String suiteListResourcePath) {
        return new QueryTestController(suiteListResourcePath);
    }

    public void runSuites(QueryTestCaseCallback callback) {
        try {
			runSuitesInternal(callback);
		} catch (IOException e) {
		    throw new UncheckedIOException("Unexpected " + e.getClass().getSimpleName(), e);
		}
    }

    private void runSuitesInternal(QueryTestCaseCallback callback) throws IOException {
        QueryTestSuiteListDescription suiteListDescription = loadYaml(suiteListResourcePath, QueryTestSuiteListDescription.class);
        for (String relativePath  : suiteListDescription.suites()) {
            String suiteResourcePath = subpath(dirname(suiteListResourcePath), relativePath);
            handleSuite(suiteResourcePath, callback);
        }
    }

    private void handleSuite(String suiteResourcePath, QueryTestCaseCallback callback) throws IOException {
        QueryTestSuiteDescription suiteDescription = loadYaml(suiteResourcePath, QueryTestSuiteDescription.class);
        for (QueryTestCaseDescription testCase : suiteDescription.cases()) {
            handleCase(suiteResourcePath, suiteDescription, testCase, callback);
        }
    }

    private void handleCase(
            String suiteResourcePath,
            QueryTestSuiteDescription suiteDescription,
            QueryTestCaseDescription testCase,
            QueryTestCaseCallback callback
            ) throws IOException {
        String caseName = testCase.name();
        TableMatcher tableMatcher = buildTableMatcher(testCase);
        ResultTable givenTable = runCase(suiteResourcePath, suiteDescription, testCase);
        ImmutableList<ImmutableList<Object>> expectedResult = loadExpectedResult(testCase);
        callback.accept(suiteResourcePath, caseName, tableMatcher, givenTable, expectedResult);
    }

    private ResultTable runCase(
            String suiteResourcePath,
            QueryTestSuiteDescription suiteDescription,
            QueryTestCaseDescription testCase) throws IOException {
        String datasetResourcePath = subpath(dirname(suiteResourcePath), suiteDescription.datasetResource());
        DatasetDescription dataset = loadYaml(datasetResourcePath, DatasetDescription.class);
        try (MiniSession session = loadSession(dataset)) {
            for (String query : suiteDescription.initQueries()) {
                session.execute(query).requireSuccess();
            }
            for (String query : testCase.initQueries()) {
                session.execute(query).requireSuccess();
            }
            return new ResultTable(session.execute(testCase.query()).requireSuccess().resultSet());
        }
    }

    private ImmutableList<ImmutableList<Object>> loadExpectedResult(QueryTestCaseDescription testCase) {
        ImmutableList<Class<?>> types = testCase.columns().map(c -> c.type());
        return testCase.expectedResult().map(r -> r.map((i, v) -> convert(v, types.get(i))));
    }

    private TableMatcher buildTableMatcher(QueryTestCaseDescription testCase) {
        return DefaultTableMatcher.of(buildTableHeaderMatcher(testCase), buildDataMatcher(testCase));
    }

    private TableHeaderMatcher buildTableHeaderMatcher(QueryTestCaseDescription testCase) {
        return DefaultTableHeaderMatcher.of(testCase.columns().map(c -> buildColumnHeaderMatcher(c)));
    }

    private ColumnHeaderMatcher buildColumnHeaderMatcher(QueryTestResultColumnDescription columnDescription) {
        String name = columnDescription.name().orElse(null);
        Class<?> type = columnDescription.type();
        Boolean nullable = columnDescription.nullable().orElse(null);
        return new ConfiguredColumnHeaderMatcher(name, type, nullable);
    }

    private DataMatcher buildDataMatcher(QueryTestCaseDescription testCase) {
        RecordMatcher recordMatcher = buildRecordMatcher(testCase);
        ImmutableList<Integer> keyPositions = collectKeyPositions(testCase.columns());
        ImmutableList<Integer> groupKeyPositions = collectGroupKeyPositions(testCase.columns());
        if (testCase.ordered()) {
            return OrderedDataMatcher.of(recordMatcher);
        } else if (!groupKeyPositions.isEmpty()) {
            DataMatcher groupDataMatcher;
            if (!keyPositions.isEmpty()) {
                groupDataMatcher = KeyedDataMatcher.of(recordMatcher, r -> keyPositions.map((i, v) -> r.get(i)));
            } else {
                groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
            }
            return GroupingDataMatcher.of(groupDataMatcher, r -> groupKeyPositions.map((i, v) -> r.get(i)));
        } else if (!keyPositions.isEmpty()) {
            return KeyedDataMatcher.of(recordMatcher, r -> keyPositions.map((i, v) -> r.get(i)));
        } else {
            return UnorderedDataMatcher.of(recordMatcher);
        }
    }

    private RecordMatcher buildRecordMatcher(QueryTestCaseDescription testCase) {
        return DefaultRecordMatcher.of(testCase.columns().map(c -> EqualityFieldMatcher.instance()));
    }

    private ImmutableList<Integer> collectKeyPositions(
            ImmutableList<QueryTestResultColumnDescription> columnDescriptions) {
        List<Integer> resultBuilder = new ArrayList<>();
        columnDescriptions.forEachIndex((i, v) -> {
            if (v.key()) {
                resultBuilder.add(i);
            }
        });
        return ImmutableList.fromCollection(resultBuilder);
    }

    private ImmutableList<Integer> collectGroupKeyPositions(
            ImmutableList<QueryTestResultColumnDescription> columnDescriptions) {
        List<Integer> resultBuilder = new ArrayList<>();
        columnDescriptions.forEachIndex((i, v) -> {
            if (v.groupKey()) {
                resultBuilder.add(i);
            }
        });
        return ImmutableList.fromCollection(resultBuilder);
    }

    private MiniSession loadSession(DatasetDescription dataset) throws IOException {
        StorageAccess storageAccess = buildStorageAccess(dataset);
        Engine engine = new SimpleEngine(new AntlrSqlParser(), new IntegratedQueryExecutor(), storageAccess);
        return new FrameworkSessionManager(engine).openSession();
    }

    private StorageAccess buildStorageAccess(DatasetDescription dataset) throws IOException {
        SimpleStorageAccess storageAccess = new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemas = storageAccess.schemas();
        for (DatasetSchemaDescription schemaDescription : dataset.schemas()) {
            Schema schema = buildSchema(schemaDescription);
            schemas.register(schema);
        }
        return storageAccess;
    }

    private Schema buildSchema(DatasetSchemaDescription schemaDescription) {
        SimpleSchema schema = new SimpleSchema(schemaDescription.name());
        SimpleResourceManager<Table> tables = schema.tables();
        for (DatasetTableDescription tableDescription : schemaDescription.tables()) {
            Table table = tableRenderer.renderTable(tableDescription);
            tables.register(table);
        }
        return schema;
    }

    private <T> T loadYaml(String resourcePath, Class<T> type) throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(resourcePath)) {
            return mapper.readValue(in, type);
        }
    }

    private InputStream openResourceInputStream(String resourcePath) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("YAML resource stream is null: " + resourcePath);
        }
        return in;
    }

    private Object convert(Object value, Class<?> targetClazz) {
        if (value == null) {
            return null;
        }
        return converter.convert(value, targetClazz);
    }

    private String dirname(String path) {
        int pos = path.lastIndexOf('/');
        return pos < 0 ? "" : path.substring(0, pos);
    }

    private String subpath(String parent, String sub) {
        return parent.isEmpty() ? sub : parent + "/" + sub;
    }

    private static final class ConfiguredColumnHeaderMatcher implements ColumnHeaderMatcher {

        private final String name;

        private final Class<?> type;

        private final Boolean nullable;

        ConfiguredColumnHeaderMatcher(String name, Class<?> type, Boolean nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }

        @Override
        public boolean isMatching(MiniColumnHeader columnHeader) {
            return
                    (name == null || columnHeader.name().equals(name)) &&
                    extractType(columnHeader) == type &&
                    (nullable == null || columnHeader.isNullable() == nullable);
        }

        @Override
        public void match(MiniColumnHeader columnHeader) {
            if (name != null && !columnHeader.name().equals(name)) {
                throw new MatchFailedException("column name: " + columnHeader.name() + " != " + name);
            }
            Class<?> givenType = extractType(columnHeader);
            if (givenType != type) {
                throw new MatchFailedException("column type: " + givenType + " != " + type);
            }
            if (nullable != null && columnHeader.isNullable() != nullable) {
                throw new MatchFailedException("column nullable: " + columnHeader.isNullable() + " != " + nullable);
            }
        }

        private Class<?> extractType(MiniColumnHeader columnHeader) {
            return StandardValueType.valueOf(columnHeader.valueDefinition().type()).clazz();
        }

    }

}
