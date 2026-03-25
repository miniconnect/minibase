package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PositionExpression implements Expression {

    private final Expression subjectExpression;

    private final Expression contextExpression;


    public PositionExpression(Expression subjectExpression, Expression contextExpression) {
        this.subjectExpression = subjectExpression;
        this.contextExpression = contextExpression;
    }


    public Expression subjectExpression() {
        return subjectExpression;
    }

    public Expression contextExpression() {
        return contextExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectExpression.parameters().concat(contextExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return LargeInteger.class;
    }

    @Override
    public boolean isNullable() {
        return subjectExpression.isNullable() || contextExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return subjectExpression.isNullable(nullabilities) || contextExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object subjectValue = subjectExpression.evaluate(values);
        if (subjectValue == null) {
            return null;
        }

        Object contextValue = contextExpression.evaluate(values);
        if (contextValue == null) {
            return null;
        }

        if (contextValue instanceof ByteString) {
            ByteString contextByteString = (ByteString) contextValue;
            ByteString subjectByteString = ByteStringUtil.byteStringify(subjectValue);
            return LargeInteger.of(contextByteString.indexOf(subjectByteString) + 1);
        } else if (contextValue instanceof BitString) {
            BitString contextBitString = (BitString) contextValue;
            BitString subjectBitString = BitStringUtil.bitStringify(subjectValue);
            return LargeInteger.of(contextBitString.indexOf(subjectBitString) + 1);
        } else {
            String contextString = StringUtil.stringify(contextValue);
            String subjectString = StringUtil.stringify(subjectValue);
            return LargeInteger.of(contextString.indexOf(subjectString) + 1);
        }
    }

    @Override
    public String automaticName() {
        return "POSITION(" + subjectExpression.automaticName() + " IN " + contextExpression.automaticName() + ")";
    }

}
