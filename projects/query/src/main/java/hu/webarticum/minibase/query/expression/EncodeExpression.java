package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.function.Function;

import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.EncodeUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class EncodeExpression implements Expression {

    public enum EncodingType {

        HEX(EncodeUtil::encodeHex),
        BASE64(EncodeUtil::encodeBase64),
        ;

        private final Function<ByteString, String> encoder;

        private EncodingType(Function<ByteString, String> encoder) {
            this.encoder = encoder;
        }

        public String encode(ByteString data) {
            return encoder.apply(data);
        }

    }


    private final Expression inputOperand;

    private final Expression encodingTypeOperand;


    public EncodeExpression(Expression inputOperand, Expression encodingTypeOperand) {
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return String.class;
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
        ByteString input = ByteStringUtil.byteStringify(inputValue);
        return encodingType.encode(input);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_encode_col" + columnIndex;
    }

}
