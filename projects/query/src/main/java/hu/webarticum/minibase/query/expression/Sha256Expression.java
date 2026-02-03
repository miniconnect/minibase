package hu.webarticum.minibase.query.expression;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class Sha256Expression implements Expression {

    private final Expression inputExpression;

    private MessageDigest messageDigest = createMessageDigest();


    public Sha256Expression(Expression inputExpression) {
        this.inputExpression = inputExpression;
    }

    private MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters();
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
        return inputExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object value = inputExpression.evaluate(values);
        if (value == null) {
            return null;
        }

        ByteString byteStringValue = ByteStringUtil.byteStringify(value);
        return ByteString.wrap(messageDigest.digest(byteStringValue.extract()));
    }

    @Override
    public String automaticName() {
        return "SHA256(" + inputExpression.automaticName() + ")";
    }

}
