package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import hu.webarticum.minibase.query.util.TemporalUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ExtractExpression implements Expression {

    public enum ExtractField {

        YEAR(ChronoField.YEAR, ChronoUnit.YEARS),
        MONTH(ChronoField.MONTH_OF_YEAR, ChronoUnit.MONTHS),
        DAY(ChronoField.DAY_OF_MONTH, ChronoUnit.DAYS),
        HOUR(ChronoField.HOUR_OF_DAY, ChronoUnit.HOURS),
        MINUTE(ChronoField.MINUTE_OF_HOUR, ChronoUnit.MINUTES),
        SECOND(ChronoField.SECOND_OF_MINUTE, ChronoUnit.SECONDS),
        TIMEZONE_HOUR(null, null),
        TIMEZONE_MINUTE(null, null),
        ;

        private final TemporalField temporalField;

        private final TemporalUnit temporalUnit;

        private ExtractField(TemporalField temporalField, TemporalUnit temporalUnit) {
            this.temporalField = temporalField;
            this.temporalUnit = temporalUnit;
        }

        public Object extractFrom(Temporal temporal) {
            if (this == TIMEZONE_HOUR || this == TIMEZONE_MINUTE) {
                if (!temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
                    return LargeInteger.ZERO;
                }
                int offsetSeconds = temporal.get(ChronoField.OFFSET_SECONDS);
                if (this == TIMEZONE_MINUTE) {
                    return LargeInteger.of((offsetSeconds / 60) % 60);
                } else {
                    return LargeInteger.of((offsetSeconds / 3600) % 24);
                }
            }
            if (!temporal.isSupported(temporalField)) {
                return this == SECOND ? BigDecimal.ZERO : LargeInteger.ZERO;
            }
            if (this == SECOND) {
                int seconds = temporal.get(ChronoField.SECOND_OF_MINUTE);
                int nanos = temporal.isSupported(ChronoField.NANO_OF_SECOND) ? temporal.get(ChronoField.NANO_OF_SECOND) : 0;
                return strip(BigDecimal.valueOf(nanos, 9)).add(BigDecimal.valueOf(seconds));
            } else {
                return LargeInteger.of(temporal.get(temporalField));
            }
        }

        public Object extractFrom(TemporalAmount temporalAmount) {
            if (this == TIMEZONE_HOUR || this == TIMEZONE_MINUTE) {
                return LargeInteger.ZERO;
            }
            List<TemporalUnit> supportedUnits = temporalAmount.getUnits();
            if (supportedUnits.contains(temporalUnit)) {
                if (this == SECOND) {
                    long seconds = temporalAmount.get(ChronoUnit.SECONDS) % 60L;
                    long nanos = supportedUnits.contains(ChronoUnit.NANOS) ? temporalAmount.get(ChronoUnit.NANOS) : 0;
                    return strip(BigDecimal.valueOf(nanos, 9)).add(BigDecimal.valueOf(seconds));
                } else {
                    return LargeInteger.of(temporalAmount.get(temporalUnit));
                }
            } else if (this == SECOND) {
                return BigDecimal.ZERO;
            } else if (!supportedUnits.contains(ChronoUnit.SECONDS)) {
                return LargeInteger.ZERO;
            }
            long seconds = temporalAmount.get(ChronoUnit.SECONDS);
            if (this == MINUTE) {
                return LargeInteger.of((seconds / 60L) % 60L);
            } else if (this == HOUR) {
                return LargeInteger.of((seconds / 3600L) % 24L);
            } else if (this == DAY) {
                return LargeInteger.of((seconds / 86400L) % 30L);
            } else if (this == MONTH) {
                return LargeInteger.of((seconds / (86400L * 30L)) % 12L);
            } else if (this == YEAR) {
                return LargeInteger.of(seconds / (86400L * 30L * 12L));
            } else {
                return LargeInteger.ZERO;
            }
        }

        public BigDecimal strip(BigDecimal value) {
            if (value.signum() == 0) {
                return BigDecimal.ZERO;
            } else {
                return value.stripTrailingZeros();
            }
        }

    }


    private final Expression inputExpression;

    private final ExtractField extractField;


    public ExtractExpression(Expression inputExpression, ExtractField extractField) {
        this.inputExpression = inputExpression;
        this.extractField = extractField;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public ExtractField extractField() {
        return extractField;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(extractField == ExtractField.SECOND ? BigDecimal.class : LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return extractField == ExtractField.SECOND ? BigDecimal.class : LargeInteger.class;
    }

    @Override
    public boolean isNullable() {
        return inputExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        if (inputValue instanceof TemporalAmount) {
            return extractField.extractFrom((TemporalAmount) inputValue);
        } else {
            return extractField.extractFrom(TemporalUtil.temporalify(inputValue));
        }
    }

    @Override
    public String automaticName() {
        return "EXTRACT(" + extractField.name() + " FROM " + inputExpression.automaticName() + ")";
    }

}
