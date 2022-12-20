package hu.webarticum.minibase.query.expression;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConcatExpression extends AbstractCompoundExpression implements FixedTypeExpression {

    public ConcatExpression(ImmutableList<Expression> subExpressions) {
        super(subExpressions);
    }

    
    @Override
    public Class<?> type() {
        return String.class;
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
