package hu.webarticum.minibase.query.expression;

import java.text.Normalizer;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ConstantExpression implements Expression {

    private static final int MAX_NORMALIZED_LENGTH = 24;


    private final Object value;


    public ConstantExpression(Object value) {
        this.value = value;
    }


    public Object value() {
        return value;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.empty();
    }

    @Override
    public Optional<Class<?>> type() {
        if (value instanceof LargeInteger) {
            return Optional.of(LargeInteger.class);
        }
        Class<?> clazz = value != null ? value.getClass() : Void.class;
        return Optional.of(clazz);
    }

    @Override
    public boolean isNullable() {
        return value == null;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return value == null;
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        if (value instanceof LargeInteger) {
            return LargeInteger.class;
        }
        return value != null ? value.getClass() : Void.class;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        return value;
    }

    @Override
    public String automaticName(int columnIndex) {
        if (value == null) {
            return "null";
        } else if (isNonPrintable(value)) {
            return "val_col" + columnIndex;
        }

        return "val_" + cleanString(value.toString());
    }

    private boolean isNonPrintable(Object value) {
        return value instanceof ByteString;
    }

    private String cleanString(String subject) {
        int length = subject.length();
        StringBuilder resultBuilder = new StringBuilder();
        boolean foundLetter = false;
        boolean wasNonLetter = false;
        int resultLength = 0;
        for (int i = 0; i < length; i++) {
            char c = subject.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (foundLetter && wasNonLetter) {
                    resultBuilder.append("_");
                    resultLength++;
                }
                resultBuilder.append(cleanChar(c));
                resultLength++;
                if (resultLength == MAX_NORMALIZED_LENGTH) {
                    break;
                }
                foundLetter = true;
                wasNonLetter = false;
            } else if (resultLength == MAX_NORMALIZED_LENGTH - 1) {
                break;
            } else {
                wasNonLetter = true;
            }
        }
        return resultBuilder.toString();
    }

    private char cleanChar(char c) {
        char result = c;
        if (result > 127) {
            result = Normalizer.normalize("" + c, Normalizer.Form.NFKD).charAt(0);
        }
        if (result > 127) {
            result = 'x';
        }
        result = Character.toLowerCase(result);
        return result;
    }

}
