package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TrimExpression implements Expression {

    public enum TrimSpecification {
        LEADING, TRAILING, BOTH;
    }


    private static final TrimSpecification DEFAULT_TRIM_SPECIFICATION = TrimSpecification.BOTH;

    private static final String DEFAULT_TRIM_CHARS = " ";

    private static final ByteString DEFAULT_TRIM_BYTES = ByteString.of(new byte[] { 0 });

    private static final BitString DEFAULT_TRIM_BITS = BitString.of(false);


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
        Class<?> subjectType = subjectOperand.type().orElse(null);
        if (subjectType == null || subjectType == ByteString.class || subjectType == BitString.class) {
            return Optional.ofNullable(subjectType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> subjectType = subjectOperand.type(typeSubstitutions);
        if (subjectType == ByteString.class || subjectType == BitString.class) {
            return subjectType;
        } else {
            return String.class;
        }
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

        Object charsValue = null;
        if (charsOperand.isPresent()) {
            charsValue = charsOperand.get().evaluate(substitutions);
            if (charsValue == null) {
                return null;
            }
        }

        TrimSpecification effectiveTrimSpecification = trimSpecification.orElse(DEFAULT_TRIM_SPECIFICATION);

        if (subjectValue instanceof ByteString) {
            return operate((ByteString) subjectValue, ByteStringUtil.byteStringify(charsValue), effectiveTrimSpecification);
        } else if (subjectValue instanceof BitString) {
            return operate((BitString) subjectValue, BitStringUtil.bitStringify(charsValue), effectiveTrimSpecification);
        } else {
            return operate(StringUtil.stringify(subjectValue), StringUtil.stringify(charsValue), effectiveTrimSpecification);
        }
    }

    private String operate(String subject, String chars, TrimSpecification trimSpecification) {
        String effectiveChars = chars != null ? chars : DEFAULT_TRIM_CHARS;
        int rightTrimPosition;
        if (trimSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(subject, effectiveChars);
        } else {
            rightTrimPosition = subject.length();
        }
        int leftTrimPosition;
        if (trimSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
            leftTrimPosition = getLeftTrimPosition(subject, effectiveChars);
        } else {
            leftTrimPosition = 0;
        }
        return subject.substring(leftTrimPosition, rightTrimPosition);
    }

    private int getLeftTrimPosition(String subject, String chars) {
        int result = 0;
        int length = subject.length();
        for (int i = 0; i < length; i++) {
            char c = subject.charAt(i);
            if (chars.indexOf(c) < 0) {
                break;
            }
            result = i + 1;
        }
        return result;
    }

    private int getRightTrimPosition(String subject, String chars) {
        int length = subject.length();
        int result = length;
        for (int i = length - 1; i >= 0; i--) {
            char c = subject.charAt(i);
            if (chars.indexOf(c) < 0) {
                break;
            }
            result = i;
        }
        return result;
    }

    private ByteString operate(ByteString subject, ByteString chars, TrimSpecification trimSpecification) {
        ByteString effectiveChars = chars != null ? chars : DEFAULT_TRIM_BYTES;
        int rightTrimPosition;
        if (trimSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(subject, effectiveChars);
        } else {
            rightTrimPosition = subject.length();
        }
        int leftTrimPosition;
        if (trimSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
            leftTrimPosition = getLeftTrimPosition(subject, effectiveChars);
        } else {
            leftTrimPosition = 0;
        }
        return subject.substring(leftTrimPosition, rightTrimPosition);
    }

    private int getLeftTrimPosition(ByteString subject, ByteString chars) {
        int result = 0;
        int length = subject.length();
        for (int i = 0; i < length; i++) {
            byte b = subject.byteAt(i);
            if (chars.indexOf(b) < 0) {
                break;
            }
            result = i + 1;
        }
        return result;
    }

    private int getRightTrimPosition(ByteString subject, ByteString chars) {
        int length = subject.length();
        int result = length;
        for (int i = length - 1; i >= 0; i--) {
            byte b = subject.byteAt(i);
            if (chars.indexOf(b) < 0) {
                break;
            }
            result = i;
        }
        return result;
    }

    private BitString operate(BitString subject, BitString chars, TrimSpecification trimSpecification) {
        BitString effectiveChars = chars != null ? chars : DEFAULT_TRIM_BITS;
        int rightTrimPosition;
        if (trimSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(subject, effectiveChars);
        } else {
            rightTrimPosition = subject.length();
        }
        int leftTrimPosition;
        if (trimSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
            leftTrimPosition = getLeftTrimPosition(subject, effectiveChars);
        } else {
            leftTrimPosition = 0;
        }
        return subject.substring(leftTrimPosition, rightTrimPosition);
    }

    private int getLeftTrimPosition(BitString subject, BitString chars) {
        int result = 0;
        int length = subject.length();
        for (int i = 0; i < length; i++) {
            boolean bit = subject.get(i);
            if (chars.indexOf(bit) < 0) {
                break;
            }
            result = i + 1;
        }
        return result;
    }

    private int getRightTrimPosition(BitString subject, BitString chars) {
        int length = subject.length();
        int result = length;
        for (int i = length - 1; i >= 0; i--) {
            boolean bit = subject.get(i);
            if (chars.indexOf(bit) < 0) {
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
