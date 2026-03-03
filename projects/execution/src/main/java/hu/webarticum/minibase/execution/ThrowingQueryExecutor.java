package hu.webarticum.minibase.execution;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniErrorException;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredError;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.lang.CheckableCloseable;

public interface ThrowingQueryExecutor extends QueryExecutor {

    @Override
    public default MiniResult execute(StorageAccess storageAccess, SessionState state, Query query) {
        try (CheckableCloseable lock = lock(storageAccess)) {
            return executeThrowing(storageAccess, state, query);
        } catch (MiniErrorException e) {
            return StoredResult.ofError(StoredError.from(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PredefinedError.QUERY_INTERRUPTED.toResult();
        }
    }

    public default CheckableCloseable lock(StorageAccess storageAccess) throws InterruptedException {
        return storageAccess.lockManager().lockExclusively();
    }

    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query);

}
