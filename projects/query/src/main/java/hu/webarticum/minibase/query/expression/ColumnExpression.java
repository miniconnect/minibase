package hu.webarticum.minibase.query.expression;

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
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return values.get(columnParameter);
    }
    
    @Override
    public String automaticName() {
        return columnParameter.columnName();
    }

}
