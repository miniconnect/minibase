package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConcatExpression extends AbstractCompoundExpression {

    public ConcatExpression(ImmutableList<Expression> subExpressions) {
        super(subExpressions);
    }


    @Override
    public Optional<Class<?>> type() {
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
    }
    
    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return false;
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean allNull = true;
        for (Expression subExpression : subExpressions) {
            Object value = subExpression.evaluate(values);
            if (value != null) {
                resultBuilder.append(value.toString());
                allNull = false;
            }
        }
        return allNull ? null : resultBuilder.toString();
    }
    
    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("CONCAT(");
        boolean first = true;
        for (Expression subExpression : subExpressions) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append(", ");
            }
            resultBuilder.append(subExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }
    
}
