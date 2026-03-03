package hu.webarticum.minibase.query.expression;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.TemporalUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class OverlapsExpression implements Expression {

    private final Expression start1Expression;

    private final Expression end1Expression;

    private final Expression start2Expression;

    private final Expression end2Expression;


    public OverlapsExpression(
            Expression start1Expression, Expression end1Expression, Expression start2Expression, Expression end2Expression) {
        this.start1Expression = start1Expression;
        this.end1Expression = end1Expression;
        this.start2Expression = start2Expression;
        this.end2Expression = end2Expression;
    }


    public Expression start1Expression() {
        return start1Expression;
    }

    public Expression end1Expression() {
        return end1Expression;
    }

    public Expression start2Expression() {
        return start2Expression;
    }

    public Expression end2Expression() {
        return end2Expression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return end1Expression.parameters()
                .concat(end1Expression.parameters())
                .concat(start2Expression.parameters())
                .concat(end2Expression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return
                start1Expression.isNullable() ||
                end1Expression.isNullable() ||
                start2Expression.isNullable() ||
                end2Expression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                start1Expression.isNullable(nullabilities) ||
                end1Expression.isNullable(nullabilities) ||
                start2Expression.isNullable(nullabilities) ||
                end2Expression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Temporal[] normalized1 = normalize(start1Expression.evaluate(values), end1Expression.evaluate(values));
        Temporal[] normalized2 = normalize(start2Expression.evaluate(values), end2Expression.evaluate(values));
        Class<?> commonType = unifyTypesOf(normalized1[0], normalized1[1], normalized2[0], normalized2[1]);
        Temporal[] ordered1 = order(
                TemporalUtil.convert(normalized1[0], commonType), TemporalUtil.convert(normalized1[1], commonType));
        Temporal[] ordered2 = order(
                TemporalUtil.convert(normalized2[0], commonType), TemporalUtil.convert(normalized2[1], commonType));
        int nullPos = findSoleNull(ordered1[0], ordered1[1], ordered2[0], ordered2[1]);
        if (nullPos == -1) {
            return (cmp(ordered1[0], ordered2[1]) < 0 && cmp(ordered2[0], ordered1[1]) < 0);
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
    public String automaticName() {
        return
                "(" + start1Expression.automaticName() + ", " + end1Expression.automaticName() +
                ") OVERLAPS (" +
                start2Expression.automaticName() + ", " + end2Expression.automaticName() + ")";
    }

}
