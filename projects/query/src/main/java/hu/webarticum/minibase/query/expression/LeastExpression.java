package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class LeastExpression implements Expression {

    private ImmutableList<Expression> parameterExpressions;
    

    public LeastExpression(ImmutableList<Expression> parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }
    
    
    public ImmutableList<Expression> parameterExpressions() {
        return parameterExpressions;
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
        ImmutableList<Class<?>> parameterTypes = parameterExpressions.map(e -> e.type().orElse(null));
        return Optional.ofNullable(UnifyUtil.unifyTypes(parameterTypes));
    }
    
    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        ImmutableList<Class<?>> parameterTypes = parameterExpressions.map(e -> e.type(types));
        Class<?> result = UnifyUtil.unifyTypes(parameterTypes);
        return result == null ? String.class : result;
    }

    @Override
    public boolean isNullable() {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (!parameterExpression.isNullable()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (!parameterExpression.isNullable(nullabilities)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        ImmutableList<Object> rawValues = parameterExpressions.map(e -> e.evaluate(values)).filter(Objects::nonNull);
        if (rawValues.isEmpty()) {
            return null;
        }
        Class<?> defaultUnifiedType = UnifyUtil.unifyTypes(rawValues.map(v -> v.getClass()));
        Class<?> unifiedType = (defaultUnifiedType != null) ? defaultUnifiedType : String.class;
        ImmutableList<Object> convertedValues = rawValues.map(v -> ConvertUtil.convert(v, unifiedType));
        int length = convertedValues.size();
        Object candidate = convertedValues.get(0);
        for (int i = 1; i < length; i++) {
            Object nextValue = convertedValues.get(i);
            if (isLessThen(nextValue, candidate)) {
                candidate = nextValue;
            }
        }
        return candidate;
    }

    private boolean isLessThen(Object nextValue, Object  existingCandidate) {
        if (nextValue instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> nextComparable = (Comparable<Object>) nextValue;
            return nextComparable.compareTo(existingCandidate) < 0;
        } else {
            return nextValue.toString().compareTo(existingCandidate.toString()) < 0;
        }
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("LEAST(");
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
