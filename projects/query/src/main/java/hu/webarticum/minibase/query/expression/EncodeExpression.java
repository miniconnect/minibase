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


    private final Expression inputExpression;

    private final Expression encodingTypeExpression;


    public EncodeExpression(Expression inputExpression, Expression encodingTypeExpression) {
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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
        ByteString input = ByteStringUtil.byteStringify(inputValue);
        return encodingType.encode(input);
    }

    @Override
    public String automaticName() {
        return "ENCODE(" + inputExpression.automaticName() + ", " + encodingTypeExpression().automaticName() + ")";
    }

}
