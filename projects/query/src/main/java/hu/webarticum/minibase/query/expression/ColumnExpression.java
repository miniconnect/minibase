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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return typeSubstitutions.get(columnParameter);
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return nullabilitySubstitutions.get(columnParameter);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        return substitutions.get(columnParameter);
    }

    @Override
    public String automaticName(int columnIndex) {
        return columnParameter.columnName();
    }

}
