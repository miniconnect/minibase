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

    private final Expression givenExpression;

    private final ImmutableList<WhenItem> whenItems;

    private final Expression elseExpression;


    public CaseExpression(Expression givenExpression, ImmutableList<WhenItem> whenItems, Expression elseExpression) {
        if (whenItems.isEmpty()) {
            throw new IllegalArgumentException("At least one when branch is required");
        }

        this.givenExpression = givenExpression;
        this.whenItems = whenItems;
        this.elseExpression = elseExpression;
    }


    public Expression givenExpression() {
        return givenExpression;
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
        if (givenExpression != null) {
            subParameters.addAll(givenExpression.parameters().asList());
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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        ImmutableList<Class<?>> branchTypes = whenItems.map(w -> w.resultExpression.type(types));
        if (elseExpression != null) {
            branchTypes = branchTypes.append(elseExpression.type(types));
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
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        if (elseExpression == null || elseExpression.isNullable(nullabilities)) {
            return true;
        }
        for (WhenItem whenItem : whenItems) {
            if (whenItem.resultExpression.isNullable(nullabilities)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        boolean hasGiven = givenExpression != null;
        Object givenValue = hasGiven ? givenExpression.evaluate(values) : null;

        for (WhenItem whenItem : whenItems) {
            Object conditionValue = whenItem.conditionExpression.evaluate(values);
            Boolean equality;
            if (hasGiven) {
                equality = ValueUtil.evalEquality(givenValue, conditionValue);
            } else {
                equality = BooleanUtil.boolify(conditionValue);
            }
            if (Boolean.TRUE.equals(equality)) {
                return whenItem.resultExpression.evaluate(values);
            }
        }

        if (elseExpression == null) {
            return null;
        }

        return elseExpression.evaluate(values);
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
