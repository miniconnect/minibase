package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SplitPartExpression implements Expression {

    private final Expression contextOperand;

    private final Expression delimiterOperand;

    private final Expression slotOperand;


    public SplitPartExpression(Expression contextOperand, Expression delimiterOperand, Expression slotOperand) {
        this.contextOperand = contextOperand;
        this.delimiterOperand = delimiterOperand;
        this.slotOperand = slotOperand;
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression delimiterOperand() {
        return delimiterOperand;
    }

    public Expression slotOperand() {
        return slotOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters().concat(delimiterOperand.parameters()).concat(slotOperand.parameters());
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
        return contextOperand.isNullable() || delimiterOperand.isNullable() || slotOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                contextOperand.isNullable(nullabilitySubstitutions) ||
                delimiterOperand.isNullable(nullabilitySubstitutions) ||
                slotOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        Object delimiterValue = delimiterOperand.evaluate(substitutions);
        if (delimiterValue == null) {
            return null;
        }

        Object slotValue = slotOperand.evaluate(substitutions);
        if (slotValue == null) {
            return null;
        }

        int slot = NumberUtil.asInt(slotValue);
        if (contextValue instanceof ByteString) {
            return operate((ByteString) contextValue, ByteStringUtil.byteStringify(delimiterValue), slot);
        } else if (contextValue instanceof BitString) {
            return operate((BitString) contextValue, BitStringUtil.bitStringify(delimiterValue), slot);
        } else {
            return operate(StringUtil.stringify(contextValue), StringUtil.stringify(delimiterValue), slot);
        }
    }

    private String operate(String context, String delimiter, int slot) {
        int length = context.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return "";
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? context.substring(pos, length) : "";
            } else if (currentSlot == slot) {
                return context.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    private ByteString operate(ByteString context, ByteString delimiter, int slot) {
        int length = context.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return ByteString.empty();
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? context.substring(pos, length) : ByteString.empty();
            } else if (currentSlot == slot) {
                return context.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    private BitString operate(BitString context, BitString delimiter, int slot) {
        int length = context.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return BitString.empty();
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? context.substring(pos, length) : BitString.empty();
            } else if (currentSlot == slot) {
                return context.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    @Override
    public String automaticName() {
        return "SPLIT_PART(" + contextOperand.automaticName() + ", " + slotOperand.automaticName() + ")";
    }

}
