package hu.webarticum.minibase.storage.impl.simple;

import hu.webarticum.minibase.storage.api.NamedResource;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class EmptyNamedResourceStore<T extends NamedResource> implements NamedResourceStore<T> {

    @Override
    public ImmutableList<String> names() {
        return ImmutableList.empty();
    }

    @Override
    public ImmutableList<T> resources() {
        return ImmutableList.empty();
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public T get(String name) {
        return null;
    }

}
