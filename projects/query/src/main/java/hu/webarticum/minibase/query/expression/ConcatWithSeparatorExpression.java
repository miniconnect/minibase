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

    private final Expression separatorOperand;

    private final ImmutableList<Expression> itemOperands;


    public ConcatWithSeparatorExpression(Expression separatorOperand, ImmutableList<Expression> itemOperands) {
        this.separatorOperand = separatorOperand;
        this.itemOperands = itemOperands;
    }


    public Expression separatorOperand() {
        return separatorOperand;
    }

    public ImmutableList<Expression> itemOperands() {
        return itemOperands;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>(separatorOperand.parameters().asList());
        for (Expression itemOperand : itemOperands) {
            subParameters.addAll(itemOperand.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        return separatorOperand.type();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return separatorOperand.type(typeSubstitutions);
    }

    @Override
    public boolean isNullable() {
        return separatorOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return separatorOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object separatorValue = separatorOperand.evaluate(substitutions);
        if (separatorValue == null) {
            return null;
        } else if (separatorValue instanceof BitString) {
            return operate(BitStringUtil.bitStringify(separatorValue), substitutions);
        } else if (separatorValue instanceof ByteString) {
            return operate(ByteStringUtil.byteStringify(separatorValue), substitutions);
        } else {
            return operate(StringUtil.stringify(separatorValue), substitutions);
        }
    }

    public Object operate(BitString separator, ImmutableMap<Parameter, Object> substitutions) {
        BitString.Builder resultBuilder = BitString.builder();
        boolean first = true;
        for (Expression itemOperand : itemOperands) {
            Object value = itemOperand.evaluate(substitutions);
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

    public Object operate(ByteString separator, ImmutableMap<Parameter, Object> substitutions) {
        ByteString.Builder resultBuilder = ByteString.builder();
        boolean first = true;
        for (Expression itemOperand : itemOperands) {
            Object value = itemOperand.evaluate(substitutions);
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

    public Object operate(String separator, ImmutableMap<Parameter, Object> substitutions) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (Expression itemOperand : itemOperands) {
            Object value = itemOperand.evaluate(substitutions);
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
    public String automaticName(int columnIndex) {
        return "expr_concat_col" + columnIndex;
    }

}
