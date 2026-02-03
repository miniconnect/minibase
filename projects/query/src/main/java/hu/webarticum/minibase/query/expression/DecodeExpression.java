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


    private final Expression inputExpression;

    private final Expression encodingTypeExpression;


    public DecodeExpression(Expression inputExpression, Expression encodingTypeExpression) {
        this.inputExpression = inputExpression;
        this.encodingTypeExpression = encodingTypeExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression encodingTypeExpression() {
        return encodingTypeExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(encodingTypeExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(ByteString.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return ByteString.class;
    }

    @Override
    public boolean isNullable() {
        return inputExpression.isNullable() || encodingTypeExpression().isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) || encodingTypeExpression().isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object encodingTypeValue = encodingTypeExpression.evaluate(values);
        if (encodingTypeValue == null) {
            return null;
        }

        String encodingTypeName = StringUtil.stringify(encodingTypeValue).toUpperCase();
        EncodingType encodingType = EncodingType.valueOf(encodingTypeName);
        String input = StringUtil.stringify(inputValue);
        return encodingType.decode(input);
    }

    @Override
    public String automaticName() {
        return "DECODE(" + inputExpression.automaticName() + ", " + encodingTypeExpression().automaticName() + ")";
    }

}
