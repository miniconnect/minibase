package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CoalesceExpression implements Expression {

    protected ImmutableList<Expression> parameterExpressions;
    

    public CoalesceExpression(ImmutableList<Expression> parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }
    
    
    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression parameterExpression : parameterExpressions) {
            subParameters.addAll(parameterExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> currentType = Void.class;
        for (Expression parameterExpression : parameterExpressions) {
            Optional<Class<?>> subType = parameterExpression.type();
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
        for (Expression parameterExpression : parameterExpressions) {
            Class<?> subType = parameterExpression.type(types);
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
            return NumberUtil.commonNumericTypeOf(currentType, type);
        } else if (CharSequence.class.isAssignableFrom(type) && CharSequence.class.isAssignableFrom(currentType)) {
            return String.class;
        } else {
            return Object.class;
        }
    }

    @Override
    public boolean isNullable() {
        for (Expression parameterExpression : parameterExpressions) {
            if (parameterExpression.isNullable()) {
                return true;
            }
        }
        return !parameterExpressions.isEmpty();
    }
    
    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression parameterExpression : parameterExpressions) {
            if (parameterExpression.isNullable(nullabilities)) {
                return true;
            }
        }
        return !parameterExpressions.isEmpty();
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        for (Expression parameterExpression : parameterExpressions) {
            Object subValue = parameterExpression.evaluate(values);
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
        for (Expression parameterExpression : parameterExpressions) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append(", ");
            }
            resultBuilder.append(parameterExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }
    
}
