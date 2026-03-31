package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RegexpReplaceExpression implements Expression {

    private static final char GLOBAL_FLAG_CHAR = 'g';


    private final Expression contextOperand;

    private final Expression patternOperand;

    private final Expression toOperand;

    private final Optional<Expression> flagsOperand;

    private final Pattern precompiledPattern;

    private final Boolean precompiledGlobal;


    public RegexpReplaceExpression(
            Expression contextOperand,
            Expression patternOperand,
            Expression toOperand,
            Optional<Expression> flagsOperand) {
        this.contextOperand = contextOperand;
        this.patternOperand = patternOperand;
        this.toOperand = toOperand;
        this.flagsOperand = flagsOperand;
        Object[] precompiledParts = precompilePatternIfPossible(patternOperand, flagsOperand);
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


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression patternOperand() {
        return patternOperand;
    }

    public Expression toOperand() {
        return toOperand;
    }

    public Optional<Expression> flagsOperand() {
        return flagsOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters()
                .concat(patternOperand.parameters())
                .concat(toOperand.parameters())
                .concat(flagsOperand.map(e -> e.parameters()).orElseGet(ImmutableList::empty));
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return String.class;
    }

    @Override
    public boolean isNullable() {
        return
                contextOperand.isNullable() ||
                patternOperand.isNullable() ||
                toOperand.isNullable() ||
                flagsOperand.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                contextOperand.isNullable(nullabilitySubstitutions) ||
                patternOperand.isNullable(nullabilitySubstitutions) ||
                toOperand.isNullable(nullabilitySubstitutions) ||
                flagsOperand.map(e -> e.isNullable(nullabilitySubstitutions)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        Object toValue = toOperand.evaluate(substitutions);
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
            if (flagsOperand.isPresent()) {
                Object flagsValue = flagsOperand.get().evaluate(substitutions);
                if (flagsValue == null) {
                    return null;
                }
                flagsString = StringUtil.stringify(flagsValue);
            }
            Object patternValue = patternOperand.evaluate(substitutions);
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
    public String automaticName(int columnIndex) {
        return "expr_replace_col" + columnIndex;
    }

}
