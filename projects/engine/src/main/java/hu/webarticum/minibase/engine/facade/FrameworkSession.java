package hu.webarticum.minibase.engine.facade;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.minibase.engine.api.EngineSession;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniError;
import hu.webarticum.miniconnect.api.MiniErrorException;
import hu.webarticum.miniconnect.api.MiniLargeDataSaveResult;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.impl.result.StoredError;
import hu.webarticum.miniconnect.impl.result.StoredLargeDataSaveResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.CheckableCloseable;

public class FrameworkSession implements MiniSession, CheckableCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(FrameworkSession.class);
    
    
    private final EngineSession engineSession;
    
    
    public FrameworkSession(EngineSession engineSession) {
        this.engineSession = engineSession;
    }
    
    
    public EngineSession engineSession() {
        return engineSession;
    }
    
    @Override
    public MiniResult execute(String sql) {
        checkClosed();
        Query query;
        try {
            query = parseAndMeasure(engineSession.sqlParser(), sql);
        } catch (Exception e) {
            logger.error("Unable to parse query string: " + sql, e);
            return new StoredResult(errorOfException(e));
        }
        return execute(query);
    }
    
    private Query parseAndMeasure(SqlParser sqlParser, String sql) {
        long startNanoTime = -1;
        if (logger.isDebugEnabled()) {
            startNanoTime = System.nanoTime();
        }
        Query query = sqlParser.parse(sql);
        if (logger.isDebugEnabled()) {
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            logger.trace("Query parsed in {}: {}", formatNanoSeconds(elapsedNanoTime), sql);
        }
        return query;
    }

    public MiniResult execute(Query query) {
        checkClosed();
        try {
            return executeThrowing(query);
        } catch (Exception e) {
            logger.error("Query execution failed", e);
            return new StoredResult(errorOfException(e));
        }
    }

    public MiniResult executeThrowing(Query query) {
        QueryExecutor queryExecutor = engineSession.queryExecutor();
        StorageAccess storageAccess = engineSession.storageAccess();
        SessionState state = engineSession.state();
        return executeAndMeasure(queryExecutor, storageAccess, state, query);
    }
    
    private MiniResult executeAndMeasure(
            QueryExecutor queryExecutor, StorageAccess storageAccess, SessionState state, Query query) {
        long startNanoTime = -1;
        if (logger.isTraceEnabled()) {
            startNanoTime = System.nanoTime();
        }
        MiniResult result = queryExecutor.execute(storageAccess, state, query);
        if (logger.isTraceEnabled()) {
            long endNanoTime = System.nanoTime();
            long elapsedNanoTime = endNanoTime - startNanoTime;
            logger.trace("Query executed in {}", formatNanoSeconds(elapsedNanoTime));
        }
        return result;
    }
    
    private String formatNanoSeconds(long nanoSeconds) {
        long seconds = nanoSeconds / 1_000_000;
        long fractionNanoSeconds = nanoSeconds % 1_000_000;
        String fractionNanoSecondsStr = "" + fractionNanoSeconds;
        int fractionLength = fractionNanoSecondsStr.length();
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(seconds);
        resultBuilder.append('.');
        for (int i = fractionLength; i < 6; i++) {
            resultBuilder.append('0');
        }
        resultBuilder.append(fractionNanoSecondsStr);
        return resultBuilder.toString();
    }

    @Override
    public MiniLargeDataSaveResult putLargeData(String variableName, long length, InputStream dataSource) {
        checkClosed();
        Exception exception;
        try {
            return putLargeDataThrowing(variableName, length, dataSource);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exception = e;
        } catch (ExecutionException e) {
            exception = (Exception) e.getCause();
        } catch (Exception e) {
            exception = e;
        }
        return new StoredLargeDataSaveResult(errorOfException(exception));
    }
    
    private MiniLargeDataSaveResult putLargeDataThrowing(
            String variableName, long length, InputStream dataSource) throws InterruptedException, ExecutionException {
        if (length > Integer.MAX_VALUE) {
            return new StoredLargeDataSaveResult(false, new StoredError(100, "00100", "Too large data"));
        }
        
        ByteString content = ByteString.fromInputStream(dataSource, (int) length);
        engineSession.state().setUserVariable(variableName, content);
        
        return new StoredLargeDataSaveResult();
    }

    @Override
    public void close() {
        engineSession.close();
    }

    @Override
    public boolean isClosed() {
        return engineSession.isClosed();
    }

    private MiniError errorOfException(Throwable exception) {
        if (!(exception instanceof MiniErrorException)) {
            String message = extractMessage(exception);
            return new StoredError(99999, "99999", message);
        }
        
        MiniErrorException errorException = (MiniErrorException) exception;
        return new StoredError(
                errorException.code(),
                errorException.sqlState(),
                errorException.getMessage());
    }
    
    private String extractMessage(Throwable exception) {
        if (exception == null) {
            return "Unknown error";
        }
        
        String message = exception.getMessage();
        if (message != null) {
            return message;
        }
        
        Throwable cause = exception.getCause();
        if (cause != null) {
            return extractMessage(cause);
        }
        
        return exception.getClass().getName();
    }

}
