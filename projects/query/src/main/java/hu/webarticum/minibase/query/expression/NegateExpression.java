package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import com.ibm.icu.math.BigDecimal;

import hu.webarticum.minibase.query.util.NumberParser;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class NegateExpression implements Expression {
    
    private final Expression subExpression;
    

    public NegateExpression(Expression subExpression) {
        this.subExpression = subExpression;
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.empty();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> subType = subExpression.type(types);
        return NumberParser.numberifyType(subType);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object subValue = subExpression.evaluate(values);
        Number subNumber = NumberParser.numberify(subValue);
        if (subNumber == null) {
            return null;
        } else if (subNumber instanceof LargeInteger) {
            return ((LargeInteger) subNumber).negate();
        } else if (subNumber instanceof BigDecimal) {
            return ((BigDecimal) subNumber).negate();
        } else {
            return -subNumber.doubleValue();
        }
    }

    @Override
    public String automaticName() {
        return "-" + subExpression.automaticName();
    }
    
}
