package hu.webarticum.minibase.storage.impl.simple;

import java.util.Optional;

import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class SimpleColumn implements Column {
    
    private final String name;
    
    private final ColumnDefinition definition;
    
    private final ImmutableList<Object> possibleValues;
    

    public SimpleColumn(String name, ColumnDefinition definition) {
        this(name, definition, null);
    }
    
    public SimpleColumn(String name, ColumnDefinition definition, ImmutableList<Object> possibleValues) {
        this.name = name;
        this.definition = definition;
        this.possibleValues = possibleValues;
    }
    

    @Override
    public String name() {
        return name;
    }

    @Override
    public ColumnDefinition definition() {
        return definition;
    }

    @Override
    public Optional<ImmutableList<Object>> possibleValues() {
        return Optional.ofNullable(possibleValues);
    }
    
}
