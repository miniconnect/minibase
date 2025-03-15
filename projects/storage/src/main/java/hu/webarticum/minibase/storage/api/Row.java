package hu.webarticum.minibase.storage.api;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public interface Row {

    public ImmutableList<String> columnNames();
    
    public Object get(int columnPosition);
    
    public Object get(String columnName);
    
    public ImmutableList<Object> getAll();
    
    public ImmutableMap<String, Object> getMap();
    
    public ImmutableMap<String, Object> getMap(ImmutableList<String> columnNames);
    
}
