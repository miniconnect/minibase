package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class UnaryRealMathFunctionExpression implements Expression {

    public enum FunctionSymbol {

        SQRT(Math::sqrt),
        CBRT(Math::cbrt),
        EXP(Math::exp),
        LN(Math::log),
        LOG10(Math::log10),
        LOG2(x -> Math.log(x) / Math.log(2.0)),
        SIN(Math::sin),
        COS(Math::cos),
        TAN(Math::tan),
        COT(x -> 1 / Math.tan(x)),
        ASIN(Math::asin),
        ACOS(Math::acos),
        ATAN(Math::atan),
        DEGREES(x -> (x / Math.PI) * 180.0),
        RADIANS(x -> (x / 180.0) * Math.PI),
        ;

        private final DoubleUnaryOperator doubleFunction;

        private FunctionSymbol(DoubleUnaryOperator doubleFunction) {
            this.doubleFunction = doubleFunction;
        }

    }

    private final FunctionSymbol functionSymbol;

    private final Expression operand;


    public UnaryRealMathFunctionExpression(FunctionSymbol functionSymbol, Expression operand) {
        this.functionSymbol = functionSymbol;
        this.operand = operand;
    }


    public FunctionSymbol functionSymbol() {
        return functionSymbol;
    }

    public Expression operand() {
        return operand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return operand.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Double.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Double.class;
    }

    @Override
    public boolean isNullable() {
        return operand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return operand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subValue = operand.evaluate(substitutions);
        if (subValue == null) {
            return null;
        }

        double doubleValue = (Double) ConvertUtil.convert(subValue, Double.class);
        return functionSymbol.doubleFunction.applyAsDouble(doubleValue);
    }

    @Override
    public String automaticName(int columnIndex) {
        return functionSymbol.name().toLowerCase() + "_" + operand.automaticName(columnIndex);
    }

}
