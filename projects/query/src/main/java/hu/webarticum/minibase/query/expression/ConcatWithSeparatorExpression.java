package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
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
        return separatorExpression.type();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return separatorExpression.type(values);
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
        } else if (separatorValue instanceof BitString) {
            return evaluateBitString(BitStringUtil.bitStringify(separatorValue), values);
        } else if (separatorValue instanceof ByteString) {
            return evaluateByteString(ByteStringUtil.byteStringify(separatorValue), values);
        } else {
            return evaluateString(StringUtil.stringify(separatorValue), values);
        }
    }

    public Object evaluateBitString(BitString separator, ImmutableMap<Parameter, Object> values) {
        BitString.Builder resultBuilder = BitString.builder();
        boolean first = true;
        for (Expression itemExpression : itemExpressions) {
            Object value = itemExpression.evaluate(values);
            if (value == null) {
                continue;
            } else if (first) {
                first = false;
            } else {
                resultBuilder.append(separator);
            }
            resultBuilder.append(BitStringUtil.bitStringify(value));
        }
        return resultBuilder.build();
    }

    public Object evaluateByteString(ByteString separator, ImmutableMap<Parameter, Object> values) {
        ByteString.Builder resultBuilder = ByteString.builder();
        boolean first = true;
        for (Expression itemExpression : itemExpressions) {
            Object value = itemExpression.evaluate(values);
            if (value == null) {
                continue;
            } else if (first) {
                first = false;
            } else {
                resultBuilder.append(separator);
            }
            resultBuilder.append(ByteStringUtil.byteStringify(value));
        }
        return resultBuilder.build();
    }

    public Object evaluateString(String separator, ImmutableMap<Parameter, Object> values) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (Expression itemExpression : itemExpressions) {
            Object value = itemExpression.evaluate(values);
            if (value == null) {
                continue;
            } else if (first) {
                first = false;
            } else {
                resultBuilder.append(separator);
            }
            resultBuilder.append(StringUtil.stringify(value));
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
