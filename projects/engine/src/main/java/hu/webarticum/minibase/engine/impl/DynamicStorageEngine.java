package hu.webarticum.minibase.engine.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

import hu.webarticum.minibase.engine.api.EngineSession;
import hu.webarticum.minibase.engine.api.TackedEngine;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.StorageAccess;

public class DynamicStorageEngine implements TackedEngine {
    
    private final SqlParser sqlParser;
    
    private final QueryExecutor queryExecutor;
    
    
    private volatile Supplier<StorageAccess> storageAccessSupplier; // NOSONAR volatile is necessary
    
    
    private volatile boolean closed = false;
    

    public DynamicStorageEngine(
            SqlParser sqlParser,
            QueryExecutor queryExecutor,
            Supplier<StorageAccess> storageAccessSupplier) {
        this.sqlParser = sqlParser;
        this.queryExecutor = queryExecutor;
        this.storageAccessSupplier = storageAccessSupplier;
    }
    

    @Override
    public EngineSession openSession() {
        return new SimpleEngineSession(this);
    }

    public SqlParser sqlParser() {
        return sqlParser;
    }
    
    public QueryExecutor queryExecutor() {
        return queryExecutor;
    }
    
    public StorageAccess storageAccess() {
        if (closed) {
            throw new IllegalArgumentException("This engine was already closed");
        }
        
        return storageAccessSupplier.get();
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        StorageAccess storageAccess = storageAccessSupplier.get();
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
    
    
    public static class StorageAccessNotReadyException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        
    }

}
