package hu.webarticum.minibase.execution;

import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.lang.CheckableCloseable;

public interface SharedThrowingQueryExecutor extends ThrowingQueryExecutor {

    @Override
    public default CheckableCloseable lock(StorageAccess storageAccess) throws InterruptedException {
        return storageAccess.lockManager().lockShared();
    }

}
