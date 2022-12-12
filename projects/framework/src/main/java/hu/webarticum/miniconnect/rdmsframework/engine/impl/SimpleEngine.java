package hu.webarticum.miniconnect.rdmsframework.engine.impl;

import java.io.IOException;
import java.io.UncheckedIOException;

import hu.webarticum.miniconnect.rdmsframework.engine.EngineSession;
import hu.webarticum.miniconnect.rdmsframework.engine.TackedEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.parser.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;

public class SimpleEngine implements TackedEngine {
    
    private final SqlParser sqlParser;
    
    private final QueryExecutor queryExecutor;
    
    private final StorageAccess storageAccess;
    

    private volatile boolean closed = false;
    
    
    public SimpleEngine(
            SqlParser sqlParser,
            QueryExecutor queryExecutor,
            StorageAccess storageAccess) {
        this.sqlParser = sqlParser;
        this.queryExecutor = queryExecutor;
        this.storageAccess = storageAccess;
    }
    

    @Override
    public EngineSession openSession() {
        return new SimpleEngineSession(this);
    }

    @Override
    public SqlParser sqlParser() {
        return sqlParser;
    }
    
    @Override
    public QueryExecutor queryExecutor() {
        return queryExecutor;
    }
    
    @Override
    public StorageAccess storageAccess() {
        return storageAccess;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (storageAccess instanceof AutoCloseable) {
            try {
                ((AutoCloseable) storageAccess).close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                IOException ioException = new IOException("Unexpected exception");
                ioException.addSuppressed(e);
                throw new UncheckedIOException(ioException);
            }
        }
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }

}
