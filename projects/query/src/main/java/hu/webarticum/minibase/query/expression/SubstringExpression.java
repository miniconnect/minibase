package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SubstringExpression implements Expression {

    private final Expression contextOperand;

    private final Optional<Expression> fromOperand;

    private final Optional<Expression> forOperand;


    public SubstringExpression(
            Expression contextOperand,
            Optional<Expression> fromOperand,
            Optional<Expression> forOperand) {
        this.contextOperand = contextOperand;
        this.fromOperand = fromOperand;
        this.forOperand = forOperand;
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Optional<Expression> fromOperand() {
        return fromOperand;
    }

    public Optional<Expression> forOperand() {
        return forOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters()
                .concat(fromOperand.map(Expression::parameters).orElseGet(ImmutableList::empty))
                .concat(forOperand.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return
                contextOperand.isNullable() ||
                fromOperand.map(Expression::isNullable).orElse(false) ||
                forOperand.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                contextOperand.isNullable(nullabilitySubstitutions) ||
                fromOperand.map(e -> e.isNullable(nullabilitySubstitutions)).orElse(false) ||
                forOperand.map(e -> e.isNullable(nullabilitySubstitutions)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        int from;
        if (fromOperand.isPresent()) {
            Object fromValue = fromOperand.get().evaluate(substitutions);
            if (fromValue == null) {
                return null;
            }
            from = (Integer) ConvertUtil.convert(fromValue, Integer.class) - 1;
        } else {
            from = 0;
        }

        Object forValue = null;
        if (forOperand.isPresent()) {
            forValue = forOperand.get().evaluate(substitutions);
            if (forValue == null) {
                return null;
            }
        }
        Integer length = (Integer) ConvertUtil.convert(forValue, Integer.class);

        if (contextValue instanceof ByteString) {
            return operate((ByteString) contextValue, from, length);
        } else if (contextValue instanceof BitString) {
            return operate((BitString) contextValue, from, length);
        } else {
            return operate(StringUtil.stringify(contextValue), from, length);
        }
    }

    private String operate(String context, int from, Integer length) {
        int[] slice = calculateSlice(context.length(), from, length);
        return context.substring(slice[0], slice[1]);
    }

    private ByteString operate(ByteString context, int from, Integer length) {
        int[] slice = calculateSlice(context.length(), from, length);
        return context.substring(slice[0], slice[1]);
    }

    private BitString operate(BitString context, int from, Integer length) {
        int[] slice = calculateSlice(context.length(), from, length);
        return context.substring(slice[0], slice[1]);
    }

    private int[] calculateSlice(int contextLength, int from, Integer sliceLength) {
        if (from >= contextLength) {
            return new int[] { contextLength, contextLength };
        }
        if (sliceLength == null) {
            return new int[] { Math.max(0, from), contextLength };
        } else if (sliceLength < 0) {
            return new int[] { 0, 0 };
        }
        int until = from + sliceLength;
        if (until < 0) {
            return new int[] { 0, 0 };
        } else {
            return new int[] { Math.max(0, from), Math.min(contextLength, until) };
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_substr_col" + columnIndex;
    }

}
