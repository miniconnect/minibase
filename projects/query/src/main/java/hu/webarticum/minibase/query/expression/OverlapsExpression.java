package hu.webarticum.minibase.query.expression;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.Optional;

import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.TemporalUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class OverlapsExpression implements Expression {

    private final Expression start1Operand;

    private final Expression end1Operand;

    private final Expression start2Operand;

    private final Expression end2Operand;


    public OverlapsExpression(
            Expression start1Operand, Expression end1Operand, Expression start2Operand, Expression end2Operand) {
        this.start1Operand = start1Operand;
        this.end1Operand = end1Operand;
        this.start2Operand = start2Operand;
        this.end2Operand = end2Operand;
    }


    public Expression start1Operand() {
        return start1Operand;
    }

    public Expression end1Operand() {
        return end1Operand;
    }

    public Expression start2Operand() {
        return start2Operand;
    }

    public Expression end2Operand() {
        return end2Operand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return end1Operand.parameters()
                .concat(end1Operand.parameters())
                .concat(start2Operand.parameters())
                .concat(end2Operand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return
                start1Operand.isNullable() ||
                end1Operand.isNullable() ||
                start2Operand.isNullable() ||
                end2Operand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                start1Operand.isNullable(nullabilitySubstitutions) ||
                end1Operand.isNullable(nullabilitySubstitutions) ||
                start2Operand.isNullable(nullabilitySubstitutions) ||
                end2Operand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object start1Value = start1Operand.evaluate(substitutions);
        Object end1Value = end1Operand.evaluate(substitutions);
        Object start2Value = start2Operand.evaluate(substitutions);
        Object end2Value = end2Operand.evaluate(substitutions);
        boolean eq1 = Objects.equals(start1Value, end1Value);
        boolean eq2 = Objects.equals(start2Value, end2Value);
        Temporal[] normalized1 = normalize(start1Value, end1Value);
        Temporal[] normalized2 = normalize(start2Value, end2Value);
        Class<?> commonType = unifyTypesOf(normalized1[0], normalized1[1], normalized2[0], normalized2[1]);
        Temporal[] ordered1 = order(
                TemporalUtil.convert(normalized1[0], commonType), TemporalUtil.convert(normalized1[1], commonType));
        Temporal[] ordered2 = order(
                TemporalUtil.convert(normalized2[0], commonType), TemporalUtil.convert(normalized2[1], commonType));
        int nullPos = findSoleNull(ordered1[0], ordered1[1], ordered2[0], ordered2[1]);
        if (nullPos == -1) {
            int cmp1 = cmp(ordered1[0], ordered2[1]);
            if (eq2 ? cmp1 > 0 : cmp1 >= 0) {
                return false;
            }
            int cmp2 = cmp(ordered2[0], ordered1[1]);
            if (eq1 ? cmp2 > 0 : cmp2 >= 0) {
                return false;
            }
            return true;
        } else if (nullPos == 0) {
            return checkHalfEnd(ordered1[1], ordered2[0], ordered2[1]);
        } else if (nullPos == 1) {
            return checkHalfEnd(ordered1[0], ordered2[0], ordered2[1]);
        } else if (nullPos == 2) {
            return checkHalfEnd(ordered2[1], ordered1[0], ordered1[1]);
        } else if (nullPos == 3) {
            return checkHalfEnd(ordered2[0], ordered1[0], ordered1[1]);
        } else {
            return null;
        }
    }

    private Temporal[] normalize(Object startValue, Object endValue) {
        Temporal startTemporal = TemporalUtil.temporalify(startValue);
        Temporal endTemporal;
        if (endValue instanceof TemporalAmount) {
            DateTimeDelta end1Delta = DateTimeDeltaUtil.deltaify(endValue);
            endTemporal = end1Delta.addToWidening(startTemporal);
        } else {
            endTemporal = TemporalUtil.temporalify(endValue);
        }
        return new Temporal[] { startTemporal, endTemporal };
    }

    private Class<?> unifyTypesOf(Temporal... temporals) {
        Class<?> type = null;
        for (Temporal temporal : temporals) {
            if (temporal != null) {
                Class<?> nextType = temporal.getClass();
                if (type == null) {
                    type = nextType;
                } else {
                    type = TemporalUtil.unifyTemporalTypes(type, nextType);
                }
            }
        }
        return type == null ? Instant.class : type;
    }

    private Temporal[] order(Temporal value1, Temporal value2) {
        if (value1 == null) {
            return new Temporal[] { value2, value1 };
        } else if (value2 == null) {
            return new Temporal[] { value1, value2 };
        } else if (cmp(value1, value2) <= 0) {
            return new Temporal[] { value1, value2 };
        } else {
            return new Temporal[] { value2, value1 };
        }
    }

    private int cmp(Temporal value1, Temporal value2) {
        @SuppressWarnings("unchecked")
        Comparable<Temporal> comparable1 = (Comparable<Temporal>) value1;
        return comparable1.compareTo(value2);
    }

    private int findSoleNull(Object... values) {
        int result = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                if (result != -1) {
                    return -2;
                }
                result = i;
            }
        }
        return result;
    }

    private Boolean checkHalfEnd(Temporal value, Temporal checkRangeStart, Temporal checkRangeEnd) {
        if (cmp(value, checkRangeStart) > 0 && cmp(value, checkRangeEnd) < 0) {
            return true;
        } else {
            return null;
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_overlaps_col" + columnIndex;
    }

}
