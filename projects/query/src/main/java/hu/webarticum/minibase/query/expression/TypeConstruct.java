package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TypeConstruct {

    public enum Symbol {

        NULL(Void.class),
        BOOLEAN(Boolean.class),
        INTEGER(LargeInteger.class), BIGINT(LargeInteger.class),
        DECIMAL(BigDecimal.class), FLOAT(Double.class),
        NVARCHAR(String.class), CLOB(String.class),
        VARBINARY(ByteString.class), BLOB(ByteString.class),
        DATE(LocalDate.class), TIME(LocalTime.class), DATETIME(LocalDateTime.class),
        INSTANT(Instant.class),
        TIMEO(OffsetTime.class), DATETIMEO(OffsetDateTime.class),
        UTCOFFSET(ZoneOffset.class),
        INTERVAL(DateTimeDelta.class),

        ;

        private final Class<?> type;

        private Symbol(Class<?> type) {
            this.type = type;
        }

        public Class<?> type() {
            return type;
        }

    }

    public enum SymbolAlias {

        NULL(Symbol.NULL),
        BOOLEAN(Symbol.BOOLEAN),
        INTEGER(Symbol.INTEGER), BIGINT(Symbol.BIGINT),
        DECIMAL(Symbol.DECIMAL), FLOAT(Symbol.FLOAT),
        NVARCHAR(Symbol.NVARCHAR), CLOB(Symbol.CLOB),
        VARBINARY(Symbol.VARBINARY), BLOB(Symbol.BLOB),
        DATE(Symbol.DATE), TIME(Symbol.TIME), DATETIME(Symbol.DATETIME),
        INSTANT(Symbol.INSTANT),
        TIMEO(Symbol.TIMEO), DATETIMEO(Symbol.DATETIMEO),
        UTCOFFSET(Symbol.UTCOFFSET),
        INTERVAL(Symbol.INTERVAL),

        BOOL(Symbol.BOOLEAN),
        TINYINT(Symbol.INTEGER), SMALLINT(Symbol.INTEGER), INT(Symbol.INTEGER),
        NUMERIC(Symbol.DECIMAL), DEC(Symbol.DECIMAL),
        REAL(Symbol.FLOAT), DOUBLE_PRECISION(Symbol.FLOAT), DOUBLE(Symbol.FLOAT),
        CHAR(Symbol.NVARCHAR), VARCHAR(Symbol.NVARCHAR), NCHAR(Symbol.NVARCHAR), TEXT(Symbol.CLOB),
        BINARY(Symbol.VARBINARY),
        TIMESTAMP(Symbol.DATETIME),
        TIME_WITHOUT_TIME_ZONE(Symbol.TIME),
        DATETIME_WITHOUT_TIME_ZONE(Symbol.DATETIME),
        TIMESTAMP_WITHOUT_TIME_ZONE(Symbol.DATETIME),
        TIMETZ(Symbol.TIMEO), TIME_WITH_TIME_ZONE(Symbol.TIMEO),
        DATETIMETZ(Symbol.DATETIMEO), DATETIME_WITH_TIME_ZONE(Symbol.DATETIMEO),
        TIMESTAMPTZ(Symbol.INSTANT), TIMESTAMP_WITH_TIME_ZONE(Symbol.INSTANT),
        TIME_WITH_OFFSET(Symbol.TIMEO), TIME_WITH_UTCOFFSET(Symbol.TIMEO),
        DATETIME_WITH_OFFSET(Symbol.DATETIMEO), DATETIME_WITH_UTCOFFSET(Symbol.DATETIMEO),
        TIMESTAMP_WITH_OFFSET(Symbol.DATETIMEO), TIMESTAMP_WITH_UTCOFFSET(Symbol.DATETIMEO),
        TIMESTAMPO(Symbol.DATETIMEO),
        TIMEZONE(Symbol.UTCOFFSET),
        ;

        private final Symbol symbol;

        private SymbolAlias(Symbol symbol) {
            this.symbol = symbol;
        }

        public Symbol symbol() {
            return symbol;
        }

    }


    private final Symbol symbol;

    private final Integer size;

    private final Integer scale;


    public TypeConstruct(SymbolAlias symbolAlias, Integer size, Integer scale) {
        this(symbolAlias.symbol(), size, scale);
    }

    public TypeConstruct(Symbol symbol, Integer size, Integer scale) {
        this.symbol = symbol;
        this.size = size;
        this.scale = scale;
    }


    public Symbol symbol() {
        return symbol;
    }

    public Integer size() {
        return size;
    }

    public Integer scale() {
        return scale;
    }

}
