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

    private final ImmutableList<Expression> operands;


    public ConcatExpression(ImmutableList<Expression> operands) {
        this.operands = operands;
    }


    public ImmutableList<Expression> parameterExpressions() {
        return operands;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression operand : operands) {
            subParameters.addAll(operand.parameters().asList());
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
        for (Expression operand : operands) {
            Class<?> nextType = operand.type().orElse(null);
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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> bestCandidate = null;
        for (Expression operand : operands) {
            Class<?> nextType = operand.type(typeSubstitutions);
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
        for (Expression operand : operands.reverseOrder()) {
            if (operand.isNullable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        for (Expression operand : operands.reverseOrder()) {
            if (operand.isNullable(nullabilitySubstitutions)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        java.util.List<Object> values = new ArrayList<>(operands.size());
        for (Expression operand : operands) {
            Object value = operand.evaluate(substitutions);
            if (value == null) {
                return null;
            }
            values.add(value);
        }
        Class<?> resultType = detectRuntimeType(values);
        if (resultType == BitString.class) {
            BitString.Builder resultBuilder = BitString.builder();
            for (Object value : values) {
                resultBuilder.append(BitStringUtil.bitStringify(value));
            }
            return resultBuilder.build();
        } else if (resultType == ByteString.class) {
            ByteString.Builder resultBuilder = ByteString.builder();
            for (Object value : values) {
                resultBuilder.append(ByteStringUtil.byteStringify(value));
            }
            return resultBuilder.build();
        } else {
            StringBuilder resultBuilder = new StringBuilder();
            for (Object value : values) {
                resultBuilder.append(StringUtil.stringify(value));
            }
            return resultBuilder.toString();
        }
    }

    private Class<?> detectRuntimeType(List<Object> values) {
        Class<?> bestCandidate = null;
        for (Object value : values) {
            Class<?> nextType = UnifyUtil.typeOf(value);
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
    public String automaticName(int columnIndex) {
        if (operands.size() == 1) {
            return operands.get(0).automaticName(columnIndex);
        } else {
            return "expr_concat_col" + columnIndex;
        }
    }

}
