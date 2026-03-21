package hu.webarticum.minibase.query.expression;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConcatExpression implements Expression {

    private final ImmutableList<Expression> parameterExpressions;


    public ConcatExpression(ImmutableList<Expression> parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }


    public ImmutableList<Expression> parameterExpressions() {
        return parameterExpressions;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression parameterExpression : parameterExpressions) {
            subParameters.addAll(parameterExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> result = null;
        for (Expression parameterExpression : parameterExpressions) {
            Class<?> nextType = parameterExpression.type().orElse(null);
            if (nextType == null) {
                return Optional.empty();
            } else if (nextType == BitString.class) {
                result = BitString.class;
            } else if (nextType == Void.class) {
                if (result == null) {
                    result = Void.class;
                }
            } else if (nextType != ByteString.class) {
                return Optional.of(String.class);
            } else if (result == null || nextType == Void.class) {
                result = ByteString.class;
            }
        }
        result = (result != null && result != Void.class) ? result : String.class;
        return Optional.ofNullable(result);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> result = null;
        for (Expression parameterExpression : parameterExpressions) {
            Class<?> nextType = parameterExpression.type(values);
            if (nextType == BitString.class) {
                result = BitString.class;
            } else if (nextType == Void.class) {
                if (result == null) {
                    result = Void.class;
                }
            } else if (nextType != ByteString.class) {
                return String.class;
            } else if (result == null || nextType == Void.class) {
                result = ByteString.class;
            }
        }
        return (result != null && result != Void.class) ? result : String.class;
    }

	@Override
    public boolean isNullable() {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (parameterExpression.isNullable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (parameterExpression.isNullable(nullabilities)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        java.util.List<Object> parameterValues = new ArrayList<>(parameterExpressions.size());
        for (Expression parameterExpression : parameterExpressions) {
            Object value = parameterExpression.evaluate(values);
            if (value == null) {
                return null;
            }
            parameterValues.add(value);
        }
        Class<?> resultType = detectRuntimeType(parameterValues);
        if (resultType == BitString.class) {
            BitString.Builder resultBuilder = BitString.builder();
            for (Object value : parameterValues) {
                resultBuilder.append(BitStringUtil.bitStringify(value));
            }
            return resultBuilder.build();
        } else if (resultType == ByteString.class) {
            ByteString.Builder resultBuilder = ByteString.builder();
            for (Object value : parameterValues) {
                resultBuilder.append(ByteStringUtil.byteStringify(value));
            }
            return resultBuilder.build();
        } else {
            StringBuilder resultBuilder = new StringBuilder();
            for (Object value : parameterValues) {
                resultBuilder.append(StringUtil.stringify(value));
            }
            return resultBuilder.toString();
        }
    }

    private Class<?> detectRuntimeType(List<Object> parameterValues) {
        Class<?> result = null;
        for (Object parameterValue : parameterValues) {
            Class<?> nextType = UnifyUtil.typeOf(parameterValue);
            if (nextType == BitString.class) {
                result = BitString.class;
            } else if (nextType != ByteString.class) {
                return String.class;
            } else if (result == null) {
                result = ByteString.class;
            }
        }
        return result != null ? result : String.class;
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("CONCAT(");
        boolean first = true;
        for (Expression parameterExpression : parameterExpressions) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append(", ");
            }
            resultBuilder.append(parameterExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }

}
