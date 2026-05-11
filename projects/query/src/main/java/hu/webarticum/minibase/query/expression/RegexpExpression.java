package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.regex.Pattern;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RegexpExpression implements Expression {

    private final Expression contextOperand;

    private final Expression patternOperand;

    private final Pattern precompiledPattern;


    public RegexpExpression(Expression contextOperand, Expression patternOperand) {
        this.contextOperand = contextOperand;
        this.patternOperand = patternOperand;
        this.precompiledPattern = precompilePatternIfPossible(patternOperand);
    }

    private static Pattern precompilePatternIfPossible(Expression patternOperand) {
        if (!patternOperand.parameters().isEmpty()) {
            return null;
        }

        Object patternValue = patternOperand.evaluate(ImmutableMap.empty());
        return compilePattern(patternValue);
    }

    private static Pattern compilePattern(Object patternValue) {
        if (patternValue == null) {
            return null;
        }

        String patternString = StringUtil.stringify(patternValue);
        return Pattern.compile(patternString);
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression patternOperand() {
        return patternOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters().concat(patternOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return contextOperand.isNullable() || patternOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return contextOperand.isNullable(nullabilitySubstitutions) || patternOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object givenValue = contextOperand.evaluate(substitutions);
        if (givenValue == null) {
            return null;
        }

        Pattern pattern = getPattern(substitutions);
        if (pattern == null) {
            return null;
        }

        String givenString = StringUtil.stringify(givenValue);
        return pattern.matcher(givenString).find();
    }

    private Pattern getPattern(ImmutableMap<Parameter, Object> substitutions) {
        if (precompiledPattern != null) {
            return precompiledPattern;
        }

        Object patternValue = patternOperand.evaluate(substitutions);
        return compilePattern(patternValue);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_regexp_col" + columnIndex;
    }

}
