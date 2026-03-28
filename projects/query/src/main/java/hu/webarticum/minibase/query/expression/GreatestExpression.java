package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class GreatestExpression implements Expression {

    private final ImmutableList<Expression> operands;


    public GreatestExpression(ImmutableList<Expression> operands) {
        this.operands = operands;
    }


    public ImmutableList<Expression> operands() {
        return operands;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression operand : operands) {
            subParameters.addAll(operand.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        ImmutableList<Class<?>> operandTypes = operands.map(e -> e.type().orElse(null));
        return Optional.ofNullable(UnifyUtil.unifyTypes(operandTypes));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        ImmutableList<Class<?>> operandTypes = operands.map(e -> e.type(typeSubstitutions));
        Class<?> result = UnifyUtil.unifyTypes(operandTypes);
        return result == null ? String.class : result;
    }

    @Override
    public boolean isNullable() {
        for (Expression operand : operands.reverseOrder()) {
            if (!operand.isNullable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        for (Expression operand : operands.reverseOrder()) {
            if (!operand.isNullable(nullabilitySubstitutions)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        ImmutableList<Object> values = operands.map(e -> e.evaluate(substitutions)).filter(Objects::nonNull);
        if (values.isEmpty()) {
            return null;
        }
        Class<?> defaultUnifiedType = UnifyUtil.unifyTypes(values.map(v -> v.getClass()));
        Class<?> unifiedType = (defaultUnifiedType != null) ? defaultUnifiedType : String.class;
        ImmutableList<Object> convertedValues = values.map(v -> ConvertUtil.convert(v, unifiedType));
        int length = convertedValues.size();
        Object candidate = convertedValues.get(0);
        for (int i = 1; i < length; i++) {
            Object nextValue = convertedValues.get(i);
            if (isGreaterThen(nextValue, candidate)) {
                candidate = nextValue;
            }
        }
        return candidate;
    }

    private boolean isGreaterThen(Object nextValue, Object  existingCandidate) {
        if (nextValue instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> nextComparable = (Comparable<Object>) nextValue;
            return nextComparable.compareTo(existingCandidate) > 0;
        } else {
            return StringUtil.stringify(nextValue).compareTo(StringUtil.stringify(existingCandidate)) > 0;
        }
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("GREATEST(");
        boolean first = true;
        for (Expression parameterExpression : operands) {
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
