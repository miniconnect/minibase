package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.BooleanUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.minibase.query.util.ValueUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CaseExpression implements Expression {

    private final Expression subjectExpression;

    private final ImmutableList<WhenItem> whenItems;

    private final Expression elseExpression;


    public CaseExpression(Expression givenExpression, ImmutableList<WhenItem> whenItems, Expression elseExpression) {
        if (whenItems.isEmpty()) {
            throw new IllegalArgumentException("At least one when branch is required");
        }

        this.subjectExpression = givenExpression;
        this.whenItems = whenItems;
        this.elseExpression = elseExpression;
    }


    public Expression givenExpression() {
        return subjectExpression;
    }

    public ImmutableList<WhenItem> whenItems() {
        return whenItems;
    }

    public Expression elseExpression() {
        return elseExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        if (subjectExpression != null) {
            subParameters.addAll(subjectExpression.parameters().asList());
        }
        for (WhenItem whenItem : whenItems) {
            subParameters.addAll(whenItem.conditionExpression.parameters().asList());
            subParameters.addAll(whenItem.resultExpression.parameters().asList());
        }
        if (elseExpression != null) {
            subParameters.addAll(elseExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        ImmutableList<Class<?>> branchTypes = whenItems.map(w -> w.resultExpression.type().orElse(null));
        if (elseExpression != null) {
            branchTypes = branchTypes.append(elseExpression.type().orElse(null));
        }
        return Optional.ofNullable(UnifyUtil.unifyTypes(branchTypes));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        ImmutableList<Class<?>> branchTypes = whenItems.map(w -> w.resultExpression.type(typeSubstitutions));
        if (elseExpression != null) {
            branchTypes = branchTypes.append(elseExpression.type(typeSubstitutions));
        }
        Class<?> result = UnifyUtil.unifyTypes(branchTypes);
        return result == null ? String.class : result;
    }

    @Override
    public boolean isNullable() {
        if (elseExpression == null || elseExpression.isNullable()) {
            return true;
        }
        for (WhenItem whenItem : whenItems) {
            if (whenItem.resultExpression.isNullable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        if (elseExpression == null || elseExpression.isNullable(nullabilitySubstitutions)) {
            return true;
        }
        for (WhenItem whenItem : whenItems) {
            if (whenItem.resultExpression.isNullable(nullabilitySubstitutions)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        boolean hasGiven = subjectExpression != null;
        Object givenValue = hasGiven ? subjectExpression.evaluate(substitutions) : null;

        for (WhenItem whenItem : whenItems) {
            Object conditionValue = whenItem.conditionExpression.evaluate(substitutions);
            Boolean equality;
            if (hasGiven) {
                equality = ValueUtil.evalEquality(givenValue, conditionValue);
            } else {
                equality = BooleanUtil.boolify(conditionValue);
            }
            if (Boolean.TRUE.equals(equality)) {
                return whenItem.resultExpression.evaluate(substitutions);
            }
        }

        if (elseExpression == null) {
            return null;
        }

        return elseExpression.evaluate(substitutions);
    }

    @Override
    public String automaticName() {
        return "WHEN expression";
    }


    public static class WhenItem {

        private final Expression conditionExpression;

        private final Expression resultExpression;


        public WhenItem(Expression conditionExpression, Expression resultExpression) {
            this.conditionExpression = conditionExpression;
            this.resultExpression = resultExpression;
        }


        public Expression conditionExpression() {
            return conditionExpression;
        }

        public Expression resultExpression() {
            return resultExpression;
        }

    }

}
