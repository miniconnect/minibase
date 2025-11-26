package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
        BINARY(ByteString.class), VARBINARY(ByteString.class), BLOB(ByteString.class),
        DATE(LocalDate.class), TIME(LocalTime.class), DATETIME(LocalDateTime.class), TIMESTAMP_WITH_TIME_ZONE(Instant.class),
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
        BINARY(Symbol.BINARY), VARBINARY(Symbol.VARBINARY), BLOB(Symbol.BLOB),
        DATE(Symbol.DATE), TIME(Symbol.TIME), DATETIME(Symbol.DATETIME), TIMESTAMP_WITH_TIME_ZONE(Symbol.TIMESTAMP_WITH_TIME_ZONE),
        INTERVAL(Symbol.INTERVAL),

        TINYINT(Symbol.INTEGER), SMALLINT(Symbol.INTEGER), INT(Symbol.INTEGER),
        NUMERIC(Symbol.DECIMAL), REAL(Symbol.FLOAT), DOUBLE_PRECISION(Symbol.FLOAT), DOUBLE(Symbol.FLOAT),
        CHAR(Symbol.NVARCHAR), VARCHAR(Symbol.NVARCHAR), NCHAR(Symbol.NVARCHAR), TEXT(Symbol.CLOB),
        TIMESTAMP(Symbol.DATETIME), TIMESTAMP_WITHOUT_TIME_ZONE(Symbol.DATETIME),

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
