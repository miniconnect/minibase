package hu.webarticum.minibase.storage.api;

public interface StorageAccess {
    
    public StorageAccessLockManager lockManager();
    
    public NamedResourceStore<Schema> schemas();

    public NamedResourceStore<Constraint> constraints();
    
    // TODO: views, triggers, procedures, sequences etc.
    
}
