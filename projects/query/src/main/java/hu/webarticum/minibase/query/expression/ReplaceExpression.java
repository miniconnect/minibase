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

    private final Expression contextOperand;

    private final Expression fromOperand;

    private final Expression toOperand;


    public ReplaceExpression(Expression contextOperand, Expression fromOperand, Expression toOperand) {
        this.contextOperand = contextOperand;
        this.fromOperand = fromOperand;
        this.toOperand = toOperand;
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression fromOperand() {
        return fromOperand;
    }

    public Expression toOperand() {
        return toOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters().concat(fromOperand.parameters()).concat(toOperand.parameters());
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
        return contextOperand.isNullable() || fromOperand.isNullable() || toOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                contextOperand.isNullable(nullabilitySubstitutions) ||
                fromOperand.isNullable(nullabilitySubstitutions) ||
                toOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        Object fromValue = fromOperand.evaluate(substitutions);
        if (fromValue == null) {
            return null;
        }

        Object toValue = toOperand.evaluate(substitutions);
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
    public String automaticName(int columnIndex) {
        return "expr_replace_col" + columnIndex;
    }

}
