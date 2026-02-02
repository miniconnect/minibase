package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.regex.Pattern;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RegexpExpression implements Expression {

    private final Expression givenExpression;

    private final Expression patternExpression;

    private final Pattern precompiledPattern;


    public RegexpExpression(Expression givenExpression, Expression patternExpression) {
        this.givenExpression = givenExpression;
        this.patternExpression = patternExpression;
        this.precompiledPattern = precompilePatternIfPossible(patternExpression);
    }

    private static Pattern precompilePatternIfPossible(Expression patternExpression) {
        if (!patternExpression.parameters().isEmpty()) {
            return null;
        }

        Object patternValue = patternExpression.evaluate(ImmutableMap.empty());
        return compilePattern(patternValue);
    }

    private static Pattern compilePattern(Object patternValue) {
        if (patternValue == null) {
            return null;
        }

        String patternString = patternValue.toString();
        return Pattern.compile(patternString);
    }


    public Expression givenExpression() {
        return givenExpression;
    }

    public Expression patternExpression() {
        return patternExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return givenExpression.parameters().concat(patternExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return givenExpression.isNullable() || patternExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return givenExpression.isNullable(nullabilities) || patternExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object givenValue = givenExpression.evaluate(values);
        if (givenValue == null) {
            return null;
        }

        Pattern pattern = getPattern(values);
        if (pattern == null) {
            return null;
        }

        String givenString = givenValue.toString();
        return pattern.matcher(givenString).find();
    }

    private Pattern getPattern(ImmutableMap<Parameter, Object> values) {
        if (precompiledPattern != null) {
            return precompiledPattern;
        }

        Object patternValue = patternExpression.evaluate(values);
        return compilePattern(patternValue);
    }

    @Override
    public String automaticName() {
        return givenExpression.automaticName() + " REGEXP " + patternExpression.automaticName();
    }

}
