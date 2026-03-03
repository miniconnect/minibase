package hu.webarticum.minibase.query.expression;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;

import hu.webarticum.miniconnect.lang.LargeInteger;

public enum SpecialValueParameter implements Parameter {

    SYSTEM_USER(String.class),

    SESSION_USER(String.class),

    CURRENT_USER(String.class),

    CURRENT_SCHEMA(String.class),

    CURRENT_CATALOG(String.class),

    CURRENT_DATE(LocalDate.class),

    CURRENT_TIME(OffsetTime.class),

    CURRENT_TIMESTAMP(Instant.class),

    READONLY(Boolean.class),

    AUTOCOMMIT(Boolean.class),

    IDENTITY(LargeInteger.class),

    LAST_INSERT_ID(LargeInteger.class),

    ;


    private final Class<?> type;

    private SpecialValueParameter(Class<?> type) {
        this.type = type;
    }


    public Class<?> type() {
        return type;
    }

}
