package hu.webarticum.minibase.storage.api;

public interface Schema extends NamedResource {

    public NamedResourceStore<Table> tables();

}
