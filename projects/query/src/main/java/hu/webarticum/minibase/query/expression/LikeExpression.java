package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.regex.Pattern;

import hu.webarticum.minibase.query.util.LikeUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class LikeExpression implements Expression {

    private final Expression givenExpression;

    private final Expression patternExpression;

    private final Expression escapeExpression;

    private final boolean caseInsensitive;

    private final Pattern precompiledPattern;


    public LikeExpression(
            Expression givenExpression, Expression patternExpression, Expression escapeExpression, boolean caseInsensitive) {
        this.givenExpression = givenExpression;
        this.patternExpression = patternExpression;
        this.escapeExpression = escapeExpression;
        this.caseInsensitive = caseInsensitive;
        this.precompiledPattern = precompilePatternIfConstant(patternExpression, escapeExpression, caseInsensitive);
    }

    private static Pattern precompilePatternIfConstant(Expression patternExpression, Expression escapeExpression, boolean caseInsensitive) {
        if (!(patternExpression instanceof ConstantExpression)) {
            return null;
        }
        if (escapeExpression != null && !(escapeExpression instanceof ConstantExpression)) {
            return null;
        }

        Object patternValue = ((ConstantExpression) patternExpression).evaluate(ImmutableMap.empty());
        Object escapeValue =
                escapeExpression != null ?
                ((ConstantExpression) escapeExpression).evaluate(ImmutableMap.empty()) :
                null;
        return compilePattern(patternValue, escapeValue, caseInsensitive);
    }

    private static Pattern compilePattern(Object patternValue, Object escapeValue, boolean caseInsensitive) {
        if (patternValue == null) {
            return null;
        }

        String patternString = patternValue.toString();
        Character escapeCharacter = getEscapeCharacter(escapeValue);

        String regexString = LikeUtil.buildRegexString(patternString, escapeCharacter);

        int flags = Pattern.DOTALL;
        if (caseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        return Pattern.compile(regexString, flags);
    }

    private static Character getEscapeCharacter(Object escapeValue) {
        if (escapeValue == null) {
            return null;
        }

        String escapeString = escapeValue.toString();
        if (escapeString.isEmpty()) {
            return null;
        }

        return escapeString.charAt(0);
    }


    public Expression givenExpression() {
        return givenExpression;
    }

    public Expression patternExpression() {
        return patternExpression;
    }

    public Expression escapeExpression() {
        return escapeExpression;
    }

    public boolean caseInsensitive() {
        return caseInsensitive;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        ImmutableList<Parameter> parameters = givenExpression.parameters().concat(patternExpression.parameters());
        if (escapeExpression == null) {
            return parameters;
        } else {
            return parameters.concat(escapeExpression.parameters());
        }
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
        return pattern.matcher(givenString).matches();
    }

    private Pattern getPattern(ImmutableMap<Parameter, Object> values) {
        if (precompiledPattern != null) {
            return precompiledPattern;
        }

        Object patternValue = patternExpression.evaluate(values);
        Object escapeValue = escapeExpression != null ? escapeExpression.evaluate(values) : null;
        return compilePattern(patternValue, escapeValue, caseInsensitive);
    }

    @Override
    public String automaticName() {
        String op = caseInsensitive ? "ILIKE" : "LIKE";
        return givenExpression.automaticName() + " " + op + " " + patternExpression.automaticName();
    }

}
