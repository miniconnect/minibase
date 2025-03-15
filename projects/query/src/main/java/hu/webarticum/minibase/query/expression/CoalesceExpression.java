package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberParser;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CoalesceExpression extends AbstractCompoundExpression {

    public CoalesceExpression(ImmutableList<Expression> subExpressions) {
        super(subExpressions);
    }
    
    
    @Override
    public Optional<Class<?>> type() {
        Class<?> currentType = Void.class;
        for (Expression subExpression : subExpressions) {
            Optional<Class<?>> subType = subExpression.type();
            if (!subType.isPresent()) {
                return Optional.empty();
            }
            currentType = mergeType(currentType, subType.get());
            if (currentType == Object.class) {
                break;
            }
        }
        return Optional.of(currentType);
    }
    
    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> currentType = Void.class;
        for (Expression subExpression : subExpressions) {
            Class<?> subType = subExpression.type(types);
            currentType = mergeType(currentType, subType);
        }
        return currentType;
    }

    private Class<?> mergeType(Class<?> currentType, Class<?> type) {
        if (type == currentType || currentType == Void.class) {
            return type;
        } else if (type == Void.class) {
            return currentType;
        } else if (Number.class.isAssignableFrom(type) && Number.class.isAssignableFrom(currentType)) {
            return NumberParser.commonNumericTypeOf(currentType, type);
        } else if (CharSequence.class.isAssignableFrom(type) && CharSequence.class.isAssignableFrom(currentType)) {
            return String.class;
        } else {
            return Object.class;
        }
    }

    @Override
    public boolean isNullable() {
        for (Expression subExpression : subExpressions) {
            if (subExpression.isNullable()) {
                return true;
            }
        }
        return !subExpressions.isEmpty();
    }
    
    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression subExpression : subExpressions) {
            if (subExpression.isNullable(nullabilities)) {
                return true;
            }
        }
        return !subExpressions.isEmpty();
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        for (Expression subExpression : subExpressions) {
            Object subValue = subExpression.evaluate(values);
            if (subValue != null) {
                return subValue;
            }
        }
        return null;
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("COALESCE(");
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
