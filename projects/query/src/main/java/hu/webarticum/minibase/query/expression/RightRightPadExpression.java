package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RightRightPadExpression implements Expression {

    private static final String DEFAULT_PAD_STRING = " ";


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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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

        String padString = DEFAULT_PAD_STRING;
        if (padStringExpression.isPresent()) {
            Object padStringValue = padStringExpression.get().evaluate(values);
            if (padStringValue == null) {
                return  null;
            }
            padString = StringUtil.stringify(padStringValue);
        }

        String inputString = StringUtil.stringify(inputValue);
        int inputLength = inputString.length();
        int length = NumberUtil.asInt(lengthValue);
        int padLength = Math.max(0, length - inputLength);
        int padStringLength = padString.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;

        StringBuilder resultBuilder = new StringBuilder(inputString);
        if (padFraction > 0) {
            resultBuilder.append(padString.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(padString);
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        return "RRPAD(" + inputExpression.automaticName() + ", " + lengthExpression.automaticName() + ")";
    }

}
