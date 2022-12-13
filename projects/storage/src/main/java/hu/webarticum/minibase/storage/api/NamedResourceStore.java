package hu.webarticum.minibase.storage.api;

import hu.webarticum.miniconnect.lang.ImmutableList;

public interface NamedResourceStore<T extends NamedResource> {

    public ImmutableList<String> names();

    public ImmutableList<T> resources();
    
    public boolean contains(String name);
    
    public T get(String name);
    
}
