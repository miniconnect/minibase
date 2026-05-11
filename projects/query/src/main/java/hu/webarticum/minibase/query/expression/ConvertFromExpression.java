package hu.webarticum.minibase.query.expression;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConvertFromExpression implements Expression {

    private final Expression inputOperand;

    private final Expression charsetNameOperand;


    public ConvertFromExpression(Expression inputOperand, Expression charsetNameOperand) {
        this.inputOperand = inputOperand;
        this.charsetNameOperand = charsetNameOperand;
    }


    public Expression inputOperand() {
        return inputOperand;
    }

    public Expression charsetNameOperand() {
        return charsetNameOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputOperand.parameters().concat(charsetNameOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return String.class;
    }

    @Override
    public boolean isNullable() {
        return inputOperand.isNullable() || charsetNameOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return inputOperand.isNullable(nullabilitySubstitutions) || charsetNameOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object inputValue = inputOperand.evaluate(substitutions);
        if (inputValue == null) {
            return null;
        }

        Object charsetNameValue = charsetNameOperand.evaluate(substitutions);
        if (charsetNameValue == null) {
            return null;
        }

        ByteString input = ByteStringUtil.byteStringify(inputValue);
        String charsetName = StringUtil.stringify(charsetNameValue);

        try {
            return new String(input.extract(), charsetName);
        } catch (UnsupportedEncodingException e) {
            // TODO: policy?
            return "";
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_convert_col" + columnIndex;
    }

}
