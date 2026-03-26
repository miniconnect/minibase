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
        /*
        Summary of the logic:
        - note: String is the default and strongest candidate plus BitString, ByteString are supported;
              the order by "strongness" is: String, BitString, ByteString
        - if there are no parameters, then the result type is String
        - if we found any known type other than BitString, ByteString, and Void, then the result type is String
        - otherwise if we found any unknown type, then the result type is unknown
        - otherwise (if one or more BitString, ByteString, and Void were exclusively found), is the result one of these:
            - if any of them is BitString, then the result type is BitString
            - else if any of them is ByteString, then the result type is ByteString
            - else (so if solely Void items were given) the result type is String
        The very last is a questionable decision, it could be Void as well,
            but for now it seems to be pausible if we don't introduce a 4th possible result type for such an edge case.
        */
        Class<?> bestCandidate = null;
        boolean foundUknown = false;
        for (Expression parameterExpression : parameterExpressions) {
            Class<?> nextType = parameterExpression.type().orElse(null);
            if (nextType == null) {
                foundUknown = true;
                continue;
            } if (nextType == BitString.class) {
                bestCandidate = BitString.class;
            } else if (nextType == ByteString.class) {
                if (nextType != BitString.class) {
                    bestCandidate = ByteString.class;
                }
            } else if (nextType == Void.class) {
                if (bestCandidate == null) {
                    bestCandidate = Void.class;
                }
            } else {
                return Optional.of(String.class);
            }
        }
        if (foundUknown) {
            return Optional.empty();
        } else if (bestCandidate == null || bestCandidate == Void.class) {
            return Optional.of(String.class);
        } else {
            return Optional.of(bestCandidate);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> bestCandidate = null;
        for (Expression parameterExpression : parameterExpressions) {
            Class<?> nextType = parameterExpression.type(values);
            if (nextType == BitString.class) {
                bestCandidate = BitString.class;
            } else if (nextType == ByteString.class) {
                if (nextType != BitString.class) {
                    bestCandidate = ByteString.class;
                }
            } else if (nextType == Void.class) {
                if (bestCandidate == null) {
                    bestCandidate = Void.class;
                }
            } else {
                return String.class;
            }
        }
        if (bestCandidate == null || bestCandidate == Void.class) {
            return String.class;
        } else {
            return bestCandidate;
        }
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
        Class<?> bestCandidate = null;
        for (Object parameterValue : parameterValues) {
            Class<?> nextType = UnifyUtil.typeOf(parameterValue);
            if (nextType == BitString.class) {
                bestCandidate = BitString.class;
            } else if (nextType == ByteString.class) {
                if (nextType != BitString.class) {
                    bestCandidate = ByteString.class;
                }
            } else if (nextType == Void.class) {
                if (bestCandidate == null) {
                    bestCandidate = Void.class;
                }
            } else {
                return String.class;
            }
        }
        if (bestCandidate == null || bestCandidate == Void.class) {
            return String.class;
        } else {
            return bestCandidate;
        }
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
