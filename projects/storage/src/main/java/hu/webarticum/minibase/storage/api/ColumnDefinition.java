package hu.webarticum.minibase.storage.api;

import java.util.Comparator;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;

public interface ColumnDefinition {

    public Class<?> clazz();
    
    public boolean isNullable();
    
    public boolean isUnique();
    
    public boolean isAutoIncremented();
    
    public Optional<ImmutableList<Object>> enumValues();
    
    public Comparator<?> comparator(); // NOSONAR
    
    public Object defaultValue();
    
}
