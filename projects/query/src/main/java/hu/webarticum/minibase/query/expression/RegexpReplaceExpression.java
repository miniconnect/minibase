package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RegexpReplaceExpression implements Expression {

    private static final char GLOBAL_FLAG_CHAR = 'g';


    private final Expression contextExpression;

    private final Expression patternExpression;

    private final Expression toExpression;

    private final Optional<Expression> flagsExpression;

    private final Pattern precompiledPattern;

    private final Boolean precompiledGlobal;


    public RegexpReplaceExpression(
            Expression contextExpression,
            Expression patternExpression,
            Expression toExpression,
            Optional<Expression> flagsExpression) {
        this.contextExpression = contextExpression;
        this.patternExpression = patternExpression;
        this.toExpression = toExpression;
        this.flagsExpression = flagsExpression;
        Object[] precompiledParts = precompilePatternIfPossible(patternExpression, flagsExpression);
        this.precompiledPattern = (Pattern) precompiledParts[0];
        this.precompiledGlobal = (Boolean) precompiledParts[1];
    }

    private static Object[] precompilePatternIfPossible(Expression patternExpression, Optional<Expression> flagsExpressionOpt) {
        if (!patternExpression.parameters().isEmpty()) {
            return new Object[] { null, null };
        }

        String flagsString = "";
        if (flagsExpressionOpt.isPresent()) {
            Expression flagsExpression = flagsExpressionOpt.get();
            if (flagsExpression.parameters().isEmpty()) {
                Object flagsValue = flagsExpression.evaluate(ImmutableMap.empty());
                if (flagsValue == null) {
                    return new Object[] { null, null };
                }
                flagsString = StringUtil.stringify(flagsValue);
            } else {
                return new Object[] { null, null };
            }
        }

        Object patternValue = patternExpression.evaluate(ImmutableMap.empty());
        if (patternValue == null) {
            return new Object[] { null, null };
        }

        String patternString = StringUtil.stringify(patternValue);
        Pattern pattern = compilePattern(patternString, flagsString);
        boolean containsGlobalFlag = containsGlobalFlag(flagsString);
        return new Object[] { pattern, containsGlobalFlag };
    }

    private static Pattern compilePattern(String patternString, String flagsString) {
        return Pattern.compile(patternString, buildFlags(flagsString));
    }

    private static boolean containsGlobalFlag(String flagsString) {
        return flagsString.indexOf(GLOBAL_FLAG_CHAR) != -1;
    }

    private static int buildFlags(String flagsString) {
        int flags = Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS;
        int length = flagsString.length();
        for (int i = 0; i < length; i++) {
            char c = flagsString.charAt(i);
            switch (c) {
                case 'i':
                    flags |= Pattern.CASE_INSENSITIVE;
                    break;
                case 'm':
                case 'w':
                    flags |= Pattern.MULTILINE;
                    break;
                case 's':
                    flags |= Pattern.DOTALL;
                    break;
                case 'x':
                    flags |= Pattern.COMMENTS;
                    break;
                default:
                    break;
            }
        }
        return flags;
    }


    public Expression contextExpression() {
        return contextExpression;
    }

    public Expression patternExpression() {
        return patternExpression;
    }

    public Expression toExpression() {
        return toExpression;
    }

    public Optional<Expression> flagsExpression() {
        return flagsExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextExpression.parameters()
                .concat(patternExpression.parameters())
                .concat(toExpression.parameters())
                .concat(flagsExpression.map(e -> e.parameters()).orElseGet(ImmutableList::empty));
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
        return
                contextExpression.isNullable() ||
                patternExpression.isNullable() ||
                toExpression.isNullable() ||
                flagsExpression.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                contextExpression.isNullable(nullabilities) ||
                patternExpression.isNullable(nullabilities) ||
                toExpression.isNullable(nullabilities) ||
                flagsExpression.map(e -> e.isNullable(nullabilities)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object contextValue = contextExpression.evaluate(values);
        if (contextValue == null) {
            return null;
        }

        Object toValue = toExpression.evaluate(values);
        if (toValue == null) {
            return null;
        }

        Pattern pattern;
        boolean isGlobal;
        if (precompiledPattern != null) {
            pattern = precompiledPattern;
            isGlobal = precompiledGlobal;
        } else {
            String flagsString = "";
            if (flagsExpression.isPresent()) {
                Object flagsValue = flagsExpression.get().evaluate(values);
                if (flagsValue == null) {
                    return null;
                }
                flagsString = StringUtil.stringify(flagsValue);
            }
            Object patternValue = patternExpression.evaluate(values);
            if (patternValue == null) {
                return null;
            }
            String patternString = StringUtil.stringify(patternValue);
            pattern = compilePattern(patternString, flagsString);
            isGlobal = containsGlobalFlag(flagsString);
        }

        String contextString = StringUtil.stringify(contextValue);
        String toString = StringUtil.stringify(toValue);

        return replace(contextString, pattern, toString, isGlobal);
    }

    private String replace(String contextString, Pattern pattern, String toString, boolean isGlobal) {
        StringBuffer resultBuffer = new StringBuffer();
        Matcher matcher = pattern.matcher(contextString);
        while (matcher.find()) {
            matcher.appendReplacement(resultBuffer, toString);
            if (!isGlobal) {
                break;
            }
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }

    @Override
    public String automaticName() {
        return "REGEXP_REPLACE(" + contextExpression.automaticName() + ", " +
                patternExpression.automaticName() + ", " +
                toExpression.automaticName() +
                (flagsExpression().map(e -> ", " + e.automaticName())).orElse("")  + ")";
    }

}
