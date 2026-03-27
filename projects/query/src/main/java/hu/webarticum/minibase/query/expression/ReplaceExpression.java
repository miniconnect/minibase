package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ReplaceExpression implements Expression {

    private final Expression contextExpression;

    private final Expression fromExpression;

    private final Expression toExpression;


    public ReplaceExpression(Expression contextExpression, Expression fromExpression, Expression toExpression) {
        this.contextExpression = contextExpression;
        this.fromExpression = fromExpression;
        this.toExpression = toExpression;
    }


    public Expression contextExpression() {
        return contextExpression;
    }

    public Expression fromExpression() {
        return fromExpression;
    }

    public Expression toExpression() {
        return toExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextExpression.parameters().concat(fromExpression.parameters()).concat(toExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> contextType = contextExpression.type().orElse(null);
        if (contextType == null || contextType == ByteString.class || contextType == BitString.class) {
            return Optional.ofNullable(contextType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> contextType = contextExpression.type(values);
        if (contextType == ByteString.class || contextType == BitString.class) {
            return contextType;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isNullable() {
        return contextExpression.isNullable() || fromExpression.isNullable() || toExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                contextExpression.isNullable(nullabilities) ||
                fromExpression.isNullable(nullabilities) ||
                toExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object contextValue = contextExpression.evaluate(values);
        if (contextValue == null) {
            return null;
        }

        Object fromValue = fromExpression.evaluate(values);
        if (fromValue == null) {
            return null;
        }

        Object toValue = toExpression.evaluate(values);
        if (toValue == null) {
            return null;
        }

        if (contextValue instanceof ByteString) {
            return operate((ByteString) contextValue, ByteStringUtil.byteStringify(fromValue), ByteStringUtil.byteStringify(toValue));
        } else if (contextValue instanceof BitString) {
            return operate((BitString) contextValue, BitStringUtil.bitStringify(fromValue), BitStringUtil.bitStringify(toValue));
        } else {
            return operate(StringUtil.stringify(contextValue), StringUtil.stringify(fromValue), StringUtil.stringify(toValue));
        }
    }

    public static String operate(String context, String from, String to) {
        /*
        This is an SQL-style alternative to String.replace(String, String).
        Unlike the latter, this is a no-op if from is empty.
        */
        int length = context.length();
        if (length == 0) {
            return "";
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        StringBuilder resultBuilder = new StringBuilder();
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(from, pos);
            if (foundIndex >= 0) {
                resultBuilder.append(context.substring(pos, foundIndex));
                resultBuilder.append(to);
                pos = foundIndex + fromLength;
            } else {
                resultBuilder.append(context.substring(pos));
                break;
            }
        }
        return resultBuilder.toString();
    }

    public static ByteString operate(ByteString context, ByteString from, ByteString to) {
        int length = context.length();
        if (length == 0) {
            return ByteString.empty();
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        ByteString.Builder resultBuilder = ByteString.builder();
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(from, pos);
            if (foundIndex >= 0) {
                resultBuilder.append(context.substring(pos, foundIndex));
                resultBuilder.append(to);
                pos = foundIndex + fromLength;
            } else {
                resultBuilder.append(context.substring(pos));
                break;
            }
        }
        return resultBuilder.build();
    }

    public static BitString operate(BitString context, BitString from, BitString to) {
        int length = context.length();
        if (length == 0) {
            return BitString.empty();
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        BitString.Builder resultBuilder = BitString.builder();
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(from, pos);
            if (foundIndex >= 0) {
                resultBuilder.append(context.substring(pos, foundIndex));
                resultBuilder.append(to);
                pos = foundIndex + fromLength;
            } else {
                resultBuilder.append(context.substring(pos));
                break;
            }
        }
        return resultBuilder.build();
    }

    @Override
    public String automaticName() {
        return "REPLACE(" + contextExpression.automaticName() + ", " +
                fromExpression.automaticName() + ", " +
                toExpression.automaticName() + ")";
    }

}
