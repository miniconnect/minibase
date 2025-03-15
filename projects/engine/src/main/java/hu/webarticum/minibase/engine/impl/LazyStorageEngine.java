package hu.webarticum.minibase.engine.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import hu.webarticum.minibase.engine.api.EngineSession;
import hu.webarticum.minibase.engine.api.TackedEngine;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.impl.simple.SimpleStorageAccess;

public class LazyStorageEngine implements TackedEngine {
    
    private final SqlParser sqlParser;
    
    private final QueryExecutor queryExecutor;
    
    
    private volatile Supplier<StorageAccess> storageAccessFactory; // NOSONAR volatile is necessary
    
    private volatile StorageAccess storageAccess = null; // NOSONAR volatile is necessary
    
    private volatile Consumer<LazyStorageEngine> onLoadedCallback = null; // NOSONAR volatile is necessary
    
    
    private volatile boolean closed = false;
    

    public LazyStorageEngine(
            SqlParser sqlParser,
            QueryExecutor queryExecutor,
            Supplier<StorageAccess> storageAccessSupplier) {
        this(sqlParser, queryExecutor, storageAccessSupplier, e -> {});
    }
    
    public LazyStorageEngine(
            SqlParser sqlParser,
            QueryExecutor queryExecutor,
            Supplier<StorageAccess> storageAccessFactory,
            Consumer<LazyStorageEngine> onLoadedCallback) {
        this.sqlParser = sqlParser;
        this.queryExecutor = queryExecutor;
        this.storageAccessFactory = storageAccessFactory;
        this.onLoadedCallback = onLoadedCallback;
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
        
        StorageAccess result = storageAccess;
        if (result != null) {
            return result;
        }
        Consumer<LazyStorageEngine> callback = onLoadedCallback;
        synchronized(this) {
            if (storageAccess == null) {
                try {
                    storageAccess = storageAccessFactory.get();
                } catch (StorageAccessNotReadyException e) {
                    return new SimpleStorageAccess();
                }
                storageAccessFactory = null;
                onLoadedCallback = null;
            }
        }
        callback.accept(this);
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
    
    
    public static class StorageAccessNotReadyException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        
    }

}
