package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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

        String inputString = StringUtil.stringify(inputValue);
        String delimiterString = StringUtil.stringify(delimiterValue);
        int slotZeroBased = NumberUtil.asInt(slotValue) - 1;

        return StringUtil.extractSlot(inputString, delimiterString, slotZeroBased);
    }

    @Override
    public String automaticName() {
        return "SPLIT_PART(" + inputExpression.automaticName() + ", " + slotExpression.automaticName() + ")";
    }

}
