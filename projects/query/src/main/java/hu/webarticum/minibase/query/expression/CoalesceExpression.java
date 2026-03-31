package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CoalesceExpression implements Expression {

    private final ImmutableList<Expression> operands;


    public CoalesceExpression(ImmutableList<Expression> operands) {
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
        for (Expression operand : operands) {
            Object value = operand.evaluate(substitutions);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String automaticName(int columnIndex) {
        if (operands.size() == 1) {
            return operands.get(0).automaticName(columnIndex);
        } else {
            return "expr_coalesce_col" + columnIndex;
        }
    }

}
