package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TrimExpression implements Expression {

    public enum TrimSpecification {
        LEADING, TRAILING, BOTH;
    }


    private static final String DEFAULT_CHARS = " ";

    private static final TrimSpecification DEFAULT_TRIM_SPECIFICATION = TrimSpecification.BOTH;


    private final Expression subjectOperand;

    private final Optional<Expression> charsOperand;

    private final Optional<TrimSpecification> trimSpecification;


    public TrimExpression(
            Expression subjectOperand,
            Optional<Expression> charsOperand,
            Optional<TrimSpecification> trimSpecification) {
        this.subjectOperand = subjectOperand;
        this.charsOperand = charsOperand;
        this.trimSpecification = trimSpecification;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Optional<Expression> charsOperand() {
        return charsOperand;
    }

    public Optional<TrimSpecification> trimSpecification() {
        return trimSpecification;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(
                charsOperand.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return subjectOperand.isNullable() || charsOperand.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                subjectOperand.isNullable(nullabilitySubstitutions) ||
                charsOperand.map(e -> e.isNullable(nullabilitySubstitutions)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }

        Object charsValue = charsOperand.orElseGet(() -> new ConstantExpression(DEFAULT_CHARS)).evaluate(substitutions);
        if (charsValue == null) {
            return null;
        }

        TrimSpecification effectiveTrimSpecification = trimSpecification.orElse(DEFAULT_TRIM_SPECIFICATION);

        String subjectString = StringUtil.stringify(subjectValue);
        String charsString = StringUtil.stringify(charsValue);
        int rightTrimPosition;
        if (effectiveTrimSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(subjectString, charsString);
        } else {
            rightTrimPosition = subjectString.length();
        }
        int leftTrimPosition;
        if (effectiveTrimSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
            leftTrimPosition = getLeftTrimPosition(subjectString, charsString);
        } else {
            leftTrimPosition = 0;
        }
        return subjectString.substring(leftTrimPosition, rightTrimPosition);
    }

    private int getLeftTrimPosition(String subjectString, String charsString) {
        int result = 0;
        int length = subjectString.length();
        for (int i = 0; i < length; i++) {
            char c = subjectString.charAt(i);
            if (charsString.indexOf(c) < 0) {
                break;
            }
            result = i + 1;
        }
        return result;
    }

    private int getRightTrimPosition(String subjectString, String charsString) {
        int length = subjectString.length();
        int result = length;
        for (int i = length - 1; i >= 0; i--) {
            char c = subjectString.charAt(i);
            if (charsString.indexOf(c) < 0) {
                break;
            }
            result = i;
        }
        return result;
    }

    @Override
    public String automaticName() {
        return "TRIM(" +
                trimSpecification.map(s -> s.name() + " ").orElse("") +
                charsOperand.map(e -> e.automaticName() + " ").orElse("") +
                (trimSpecification.isPresent() || charsOperand.isPresent() ? "FROM " : "") +
                subjectOperand.automaticName() + ")";
    }

}
