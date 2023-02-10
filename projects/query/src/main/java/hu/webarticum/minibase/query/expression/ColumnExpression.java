package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ColumnExpression implements Expression {

    private final ColumnParameter columnParameter;
    
    
    public ColumnExpression(String tableAlias, String columnName) {
        this.columnParameter = new ColumnParameter(tableAlias, columnName);
    }
    
    
    public ColumnParameter columnParameter() {
        return columnParameter;
    }
    
    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.of(columnParameter);
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.empty();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return types.get(columnParameter);
    }
    
    @Override
    public boolean isNullable() {
        return true;
    }
    
    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return nullabilities.get(columnParameter);
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return values.get(columnParameter);
    }
    
    @Override
    public String automaticName() {
        return columnParameter.columnName();
    }

}
