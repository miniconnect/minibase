package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.minibase.query.util.ValueUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class InExpression implements Expression {

    private final Expression subjectOperand;

    private final ImmutableList<Expression> contextListOperand;


    public InExpression(Expression subjectOperand, ImmutableList<Expression> contextListOperand) {
        this.subjectOperand = subjectOperand;
        this.contextListOperand = contextListOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public ImmutableList<Expression> contextListOperand() {
        return contextListOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        subParameters.addAll(subjectOperand.parameters().asList());
        for (Expression itemExpression : contextListOperand) {
            subParameters.addAll(itemExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        if (subjectOperand.isNullable()) {
            return true;
        }
        for (Expression listedExpression : contextListOperand) {
            if (listedExpression.isNullable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        if (subjectOperand.isNullable(nullabilitySubstitutions)) {
            return true;
        }
        for (Expression itemExpression : contextListOperand) {
            if (itemExpression.isNullable(nullabilitySubstitutions)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }
        Class<?> subjectType = UnifyUtil.typeOf(subjectValue);
        boolean foundNull = false;
        for (Expression itemExpression : contextListOperand) {
            Object itemValue = itemExpression.evaluate(substitutions);
            if (itemValue == null) {
                foundNull = true;
                continue;
            }
            Object convertedItemValue = ConvertUtil.convert(itemValue, subjectType);
            if (ValueUtil.evalEquality(subjectValue, convertedItemValue)) {
                return true;
            }
        }
        return foundNull ? null : false;
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_in_col" + columnIndex;
    }

}
