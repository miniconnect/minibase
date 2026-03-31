package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.function.Function;

import hu.webarticum.minibase.query.util.EncodeUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class DecodeExpression implements Expression {

    public enum EncodingType {

        HEX(EncodeUtil::decodeHex),
        BASE64(EncodeUtil::decodeBase64),
        ;

        private final Function<String, ByteString> decoder;

        private EncodingType(Function<String, ByteString> decoder) {
            this.decoder = decoder;
        }

        public ByteString decode(String encodedData) {
            return decoder.apply(encodedData);
        }

    }


    private final Expression inputOperand;

    private final Expression encodingTypeOperand;


    public DecodeExpression(Expression inputOperand, Expression encodingTypeOperand) {
        this.inputOperand = inputOperand;
        this.encodingTypeOperand = encodingTypeOperand;
    }


    public Expression inputOperand() {
        return inputOperand;
    }

    public Expression encodingTypeOperand() {
        return encodingTypeOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputOperand.parameters().concat(encodingTypeOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(ByteString.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return ByteString.class;
    }

    @Override
    public boolean isNullable() {
        return inputOperand.isNullable() || encodingTypeOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return inputOperand.isNullable(nullabilitySubstitutions) || encodingTypeOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object inputValue = inputOperand.evaluate(substitutions);
        if (inputValue == null) {
            return null;
        }

        Object encodingTypeValue = encodingTypeOperand.evaluate(substitutions);
        if (encodingTypeValue == null) {
            return null;
        }

        String encodingTypeName = StringUtil.stringify(encodingTypeValue).toUpperCase();
        EncodingType encodingType = EncodingType.valueOf(encodingTypeName);
        String input = StringUtil.stringify(inputValue);
        return encodingType.decode(input);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_decode_col" + columnIndex;
    }

}
