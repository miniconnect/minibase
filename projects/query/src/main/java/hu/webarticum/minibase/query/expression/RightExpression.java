package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RightExpression implements Expression {

    private final Expression contextOperand;

    private final Expression lengthOperand;


    public RightExpression(Expression contextOperand, Expression lengthOperand) {
        this.contextOperand = contextOperand;
        this.lengthOperand = lengthOperand;
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression lengthOperand() {
        return lengthOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters().concat(lengthOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> contextType = contextOperand.type().orElse(null);
        if (contextType == null || contextType == ByteString.class || contextType == BitString.class) {
            return Optional.ofNullable(contextType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> contextType = contextOperand.type(typeSubstitutions);
        if (contextType == ByteString.class || contextType == BitString.class) {
            return contextType;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isNullable() {
        return contextOperand.isNullable() || lengthOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return contextOperand.isNullable(nullabilitySubstitutions) || lengthOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        Object lengthValue = lengthOperand.evaluate(substitutions);
        if (lengthValue == null) {
            return null;
        }
        int length = NumberUtil.asInt(lengthValue);

        if (contextValue instanceof ByteString) {
            return operate((ByteString) contextValue, length);
        } else if (contextValue instanceof BitString) {
            return operate((BitString) contextValue, length);
        } else {
            return operate(StringUtil.stringify(contextValue), length);
        }
    }

    private String operate(String context, int length) {
        int contextLength = context.length();
        if (length >= contextLength) {
            return context;
        }
        if (length < 0) {
            length = contextLength + length;
        }
        return length > 0 ? context.substring(contextLength - length) : "";
    }

    private ByteString operate(ByteString context, int length) {
        int contextLength = context.length();
        if (length >= contextLength) {
            return context;
        }
        if (length < 0) {
            length = contextLength + length;
        }
        return length > 0 ? context.substring(contextLength - length) : ByteString.empty();
    }

    private BitString operate(BitString context, int length) {
        int contextLength = context.length();
        if (length >= contextLength) {
            return context;
        }
        if (length < 0) {
            length = contextLength + length;
        }
        return length > 0 ? context.substring(contextLength - length) : BitString.empty();
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_right_col" + columnIndex;
    }

}
