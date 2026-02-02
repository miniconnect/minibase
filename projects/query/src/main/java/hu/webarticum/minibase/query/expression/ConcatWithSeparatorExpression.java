package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConcatWithSeparatorExpression implements Expression {

    private final Expression separatorExpression;

    private final ImmutableList<Expression> itemExpressions;


    public ConcatWithSeparatorExpression(Expression separatorExpression, ImmutableList<Expression> itemExpressions) {
        this.separatorExpression = separatorExpression;
        this.itemExpressions = itemExpressions;
    }


    public Expression separatorExpression() {
        return separatorExpression;
    }

    public ImmutableList<Expression> itemExpressions() {
        return itemExpressions;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>(separatorExpression.parameters().asList());
        for (Expression itemExpression : itemExpressions) {
            subParameters.addAll(itemExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
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
        return separatorExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return separatorExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object separatorValue = separatorExpression.evaluate(values);
        if (separatorValue == null) {
            return null;
        }

        String separatorString = StringUtil.stringify(separatorValue);

        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (Expression itemExpression : itemExpressions) {
            Object itemValue = itemExpression.evaluate(values);
            if (itemValue == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                resultBuilder.append(separatorString);
            }
            String itemString = StringUtil.stringify(itemValue);
            resultBuilder.append(itemString);
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("CONCAT_WS(");
        resultBuilder.append(separatorExpression.automaticName());
        for (Expression itemExpression : itemExpressions) {
            resultBuilder.append(", ");
            resultBuilder.append(itemExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }

}
