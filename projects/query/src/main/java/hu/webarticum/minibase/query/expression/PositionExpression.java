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

    private final Expression subjectOperand;

    private final Expression contextOperand;


    public PositionExpression(Expression subjectOperand, Expression contextOperand) {
        this.subjectOperand = subjectOperand;
        this.contextOperand = contextOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression contextOperand() {
        return contextOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(contextOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return LargeInteger.class;
    }

    @Override
    public boolean isNullable() {
        return subjectOperand.isNullable() || contextOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return subjectOperand.isNullable(nullabilitySubstitutions) || contextOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }

        Object contextValue = contextOperand.evaluate(substitutions);
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
    public String automaticName(int columnIndex) {
        return "expr_position_col" + columnIndex;
    }

}
