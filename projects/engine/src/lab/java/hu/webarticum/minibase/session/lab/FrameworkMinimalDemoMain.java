package hu.webarticum.minibase.session.lab;

import java.io.IOException;
import java.time.LocalDateTime;

import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.api.EngineSession;
import hu.webarticum.minibase.engine.facade.FrameworkSession;
import hu.webarticum.minibase.engine.impl.SimpleEngine;
import hu.webarticum.minibase.query.execution.QueryExecutor;
import hu.webarticum.minibase.query.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.minibase.query.parser.AntlrSqlParser;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.minibase.storage.impl.simple.SimpleResourceManager;
import hu.webarticum.minibase.storage.impl.simple.SimpleSchema;
import hu.webarticum.minibase.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;
import hu.webarticum.miniconnect.repl.PlainAnsiAppendable;
import hu.webarticum.miniconnect.repl.ResultSetPrinter;

public class FrameworkMinimalDemoMain {

    public static void main(String[] args) throws IOException {
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new IntegratedQueryExecutor();
        StorageAccess storageAccess = createStorageAccess();
        try (
                Engine engine = new SimpleEngine(sqlParser, queryExecutor, storageAccess);
                EngineSession engineSession = engine.openSession();
                MiniSession session = new FrameworkSession(engineSession)) {
            MiniResult result = session.execute(
                    //"SELECT id, label AS apple, description banana FROM data " +
                    //        "WHERE label='Lorem' AND description='Hello' " +
                    //        "ORDER BY id DESC");
                    //"DELETE FROM data WHERE a=1 AND b='banana'");
                    //"UPDATE data SET col1=NULL, col2=99, col3='str' WHERE a=1 AND b='banana'");
                    //"INSERT INTO data (id, label, description) VALUES (1, 'banana', NULL)");
                    "SHOW SCHEMAS LIKE 'd%'");
                    //"SHOW TABLES LIKE 'd%'");
            if (!result.success()) {
                System.out.println("oops");
                System.out.println(result.error().message());
            } else {
                System.out.println("OK");
                new ResultSetPrinter().print(new ResultTable(result.resultSet()), new PlainAnsiAppendable(System.out));
            }
        }
    }
    
    public static StorageAccess createStorageAccess() {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        SimpleSchema schema = new SimpleSchema("default");
        schemaManager.register(schema);
        SimpleResourceManager<Table> tableManager = schema.tables();
        Table dataTable = SimpleTable.builder()
                .name("data")
                .addColumnWithIndex("id", new SimpleColumnDefinition(Integer.class))
                .addColumnWithIndex("label", new SimpleColumnDefinition(String.class))
                .addColumnWithIndex("description", new SimpleColumnDefinition(String.class))
                .addRow(ImmutableList.of(1, "Lorem", "Hello"))
                .addRow(ImmutableList.of(2, "Lorem", "World"))
                .addRow(ImmutableList.of(3, "Lorem", "Hello"))
                .addRow(ImmutableList.of(4, "Ipsum", "Hello"))
                .addRow(ImmutableList.of(5, "Ipsum", "World"))
                .build();
        tableManager.register(dataTable);
        Table anotherTable = SimpleTable.builder()
                .name("another")
                .addColumnWithIndex("id", new SimpleColumnDefinition(Integer.class))
                .addColumnWithIndex("datetime", new SimpleColumnDefinition(LocalDateTime.class))
                .addRow(ImmutableList.of(1, LocalDateTime.now()))
                .addRow(ImmutableList.of(1, LocalDateTime.now().minusDays(5)))
                .build();
        tableManager.register(anotherTable);
        return storageAccess;
    }
    
}
