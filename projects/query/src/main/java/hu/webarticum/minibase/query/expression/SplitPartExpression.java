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

    private final Expression inputExpression;

    private final Expression delimiterExpression;

    private final Expression slotExpression;


    public SplitPartExpression(Expression inputExpression, Expression delimiterExpression, Expression slotExpression) {
        this.inputExpression = inputExpression;
        this.delimiterExpression = delimiterExpression;
        this.slotExpression = slotExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression delimiterExpression() {
        return delimiterExpression;
    }

    public Expression slotExpression() {
        return slotExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(delimiterExpression.parameters()).concat(slotExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> inputType = inputExpression.type().orElse(null);
        if (inputType == null || inputType == ByteString.class || inputType == BitString.class) {
            return Optional.ofNullable(inputType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> inputType = inputExpression.type(values);
        if (inputType == ByteString.class || inputType == BitString.class) {
            return inputType;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isNullable() {
        return inputExpression.isNullable() || delimiterExpression.isNullable() || slotExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                inputExpression.isNullable(nullabilities) ||
                delimiterExpression.isNullable(nullabilities) ||
                slotExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object delimiterValue = delimiterExpression.evaluate(values);
        if (delimiterValue == null) {
            return null;
        }

        Object slotValue = slotExpression.evaluate(values);
        if (slotValue == null) {
            return null;
        }

        int slot = NumberUtil.asInt(slotValue);
        if (inputValue instanceof ByteString) {
            return operate((ByteString) inputValue, ByteStringUtil.byteStringify(delimiterValue), slot);
        } else if (inputValue instanceof BitString) {
            return operate((BitString) inputValue, BitStringUtil.bitStringify(delimiterValue), slot);
        } else {
            return operate(StringUtil.stringify(inputValue), StringUtil.stringify(delimiterValue), slot);
        }
    }

    private String operate(String input, String delimiter, int slot) {
        int length = input.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return "";
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = input.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? input.substring(pos, length) : "";
            } else if (currentSlot == slot) {
                return input.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    private ByteString operate(ByteString input, ByteString delimiter, int slot) {
        int length = input.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return ByteString.empty();
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = input.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? input.substring(pos, length) : ByteString.empty();
            } else if (currentSlot == slot) {
                return input.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    private BitString operate(BitString input, BitString delimiter, int slot) {
        int length = input.length();
        int delimiterLength = delimiter.length();
        if (length == 0 || delimiterLength == 0 || slot <= 0) {
            return BitString.empty();
        }

        int currentSlot = 1;
        int pos = 0;
        while (true) {
            int foundIndex = input.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? input.substring(pos, length) : BitString.empty();
            } else if (currentSlot == slot) {
                return input.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    @Override
    public String automaticName() {
        return "SPLIT_PART(" + inputExpression.automaticName() + ", " + slotExpression.automaticName() + ")";
    }

}
