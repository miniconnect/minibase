package hu.webarticum.minibase.storage.impl.simple;

import hu.webarticum.minibase.storage.api.Constraint;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;

// TODO
public class SimpleStorageAccess implements StorageAccess {

    private final SimpleResourceManager<Schema> schemaManager = new SimpleResourceManager<>();
    
    private final SimpleStorageAccessLockManager lockManager = new SimpleStorageAccessLockManager();
    

    @Override
    public SimpleStorageAccessLockManager lockManager() {
        return lockManager;
    }
    
    @Override
    public SimpleResourceManager<Schema> schemas() {
        return schemaManager;
    }

    @Override
    public NamedResourceStore<Constraint> constraints() {
        return new EmptyNamedResourceStore<>();
    }
    
}
