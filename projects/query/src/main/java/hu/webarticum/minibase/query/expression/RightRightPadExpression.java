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

public class RightRightPadExpression implements Expression {

    private final Expression inputExpression;

    private final Expression lengthExpression;

    private final Optional<Expression> padStringExpression;


    public RightRightPadExpression(
            Expression inputExpression,
            Expression lengthExpression,
            Optional<Expression> padStringExpression) {
        this.inputExpression = inputExpression;
        this.lengthExpression = lengthExpression;
        this.padStringExpression = padStringExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression lengthExpression() {
        return lengthExpression;
    }

    public Optional<Expression> padStringExpression() {
        return padStringExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters()
                .concat(lengthExpression.parameters())
                .concat(padStringExpression.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return
                inputExpression.isNullable() ||
                lengthExpression.isNullable() ||
                padStringExpression.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                inputExpression.isNullable(nullabilities) ||
                lengthExpression.isNullable(nullabilities) ||
                padStringExpression.map(e -> e.isNullable(nullabilities)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object lengthValue = lengthExpression.evaluate(values);
        if (lengthValue == null) {
            return null;
        }

        Object padValue = null;
        if (padStringExpression.isPresent()) {
            padValue = padStringExpression.get().evaluate(values);
            if (padValue == null) {
                return  null;
            }
        }

        int length = NumberUtil.asInt(lengthValue);
        if (inputValue instanceof ByteString) {
            return operate((ByteString) inputValue, length, ByteStringUtil.byteStringify(padValue));
        } else if (inputValue instanceof BitString) {
            return operate((BitString) inputValue, length, BitStringUtil.bitStringify(padValue));
        } else {
            return operate(StringUtil.stringify(inputValue), length, StringUtil.stringify(padValue));
        }
    }

    private String operate(String input, int length, String pad) {
        String effectivePad = pad != null ? pad : " ";
        int inputLength = input.length();
        int padLength = Math.max(0, length - inputLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(input);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.toString();
    }

    private ByteString operate(ByteString input, int length, ByteString pad) {
        ByteString effectivePad = pad != null ? pad : ByteString.ofByte(0);
        int inputLength = input.length();
        int padLength = Math.max(0, length - inputLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        ByteString.Builder resultBuilder = ByteString.builder();
        resultBuilder.append(input);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.build();
    }

    private BitString operate(BitString input, int length, BitString pad) {
        BitString effectivePad = pad != null ? pad : BitString.of("0");
        int inputLength = input.length();
        int padLength = Math.max(0, length - inputLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        BitString.Builder resultBuilder = BitString.builder();
        resultBuilder.append(input);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.build();
    }

    @Override
    public String automaticName() {
        return "RRPAD(" + inputExpression.automaticName() + ", " + lengthExpression.automaticName() + ")";
    }

}
