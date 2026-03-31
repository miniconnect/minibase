package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RightRightPadExpression implements Expression {

    private final Expression subjectOperand;

    private final Expression lengthOperand;

    private final Optional<Expression> padStringOperand;


    public RightRightPadExpression(
            Expression subjectOperand,
            Expression lengthOperand,
            Optional<Expression> padStringOperand) {
        this.subjectOperand = subjectOperand;
        this.lengthOperand = lengthOperand;
        this.padStringOperand = padStringOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression lengthOperand() {
        return lengthOperand;
    }

    public Optional<Expression> padStringOperand() {
        return padStringOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters()
                .concat(lengthOperand.parameters())
                .concat(padStringOperand.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return
                subjectOperand.isNullable() ||
                lengthOperand.isNullable() ||
                padStringOperand.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                subjectOperand.isNullable(nullabilitySubstitutions) ||
                lengthOperand.isNullable(nullabilitySubstitutions) ||
                padStringOperand.map(e -> e.isNullable(nullabilitySubstitutions)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }

        Object lengthValue = lengthOperand.evaluate(substitutions);
        if (lengthValue == null) {
            return null;
        }

        Object padValue = null;
        if (padStringOperand.isPresent()) {
            padValue = padStringOperand.get().evaluate(substitutions);
            if (padValue == null) {
                return  null;
            }
        }

        int length = NumberUtil.asInt(lengthValue);
        if (subjectValue instanceof ByteString) {
            return operate((ByteString) subjectValue, length, ByteStringUtil.byteStringify(padValue));
        } else if (subjectValue instanceof BitString) {
            return operate((BitString) subjectValue, length, BitStringUtil.bitStringify(padValue));
        } else {
            return operate(StringUtil.stringify(subjectValue), length, StringUtil.stringify(padValue));
        }
    }

    private String operate(String subject, int length, String pad) {
        String effectivePad = pad != null ? pad : " ";
        int subjectLength = subject.length();
        int padLength = Math.max(0, length - subjectLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(subject);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.toString();
    }

    private ByteString operate(ByteString subject, int length, ByteString pad) {
        ByteString effectivePad = pad != null ? pad : ByteString.ofByte(0);
        int subjectLength = subject.length();
        int padLength = Math.max(0, length - subjectLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        ByteString.Builder resultBuilder = ByteString.builder();
        resultBuilder.append(subject);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.build();
    }

    private BitString operate(BitString subject, int length, BitString pad) {
        BitString effectivePad = pad != null ? pad : BitString.of("0");
        int subjectLength = subject.length();
        int padLength = Math.max(0, length - subjectLength);
        int padStringLength = effectivePad.length();
        int padRepeats = padLength / padStringLength;
        int padFraction = padLength % padStringLength;
        BitString.Builder resultBuilder = BitString.builder();
        resultBuilder.append(subject);
        if (padFraction > 0) {
            resultBuilder.append(effectivePad.substring(padStringLength - padFraction));
        }
        for (int i = 0; i < padRepeats; i++) {
            resultBuilder.append(effectivePad);
        }
        return resultBuilder.build();
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_pad_col" + columnIndex;
    }

}
