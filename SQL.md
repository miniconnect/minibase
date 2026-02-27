## MiniBase SQL support

## Overview

MiniBase supports a subset of SQL described in this document.
The syntax is mostly but not entirely based on the SQL 92 Standard, and is heavily inspired by PostgreSQL and MySQL.
Some of the supported features:

- schema switching, user variable handling, and other session operations
- CRUD queries (`INSERT`, `SELECT`, `UPDATE`, `DELETE`, `REPLACE`)
- arbitrarily composable expressions
- filtering with `WHERE`
- multi-table queries using `INNER JOIN` and `LEFT JOIN`
- `ORDER BY`, with support for `ASC`/`DESC` and `NULLS` `FIRST`/`LAST`
- `COUNT` queries, with filtering support
- advanced date/time handling
- arbitrarily large integers, decimals and floating point numbers, mathematical functions

[You can find the all-in-one SQL grammar file here.](projects/query/src/main/antlr/hu/webarticum/minibase/query/query/antlr/grammar/SqlQuery.g4)

## Limitations

Here is some of the major limitations:

- limited `WHERE` filters (currently, only simple column filters supported, connected with `AND` operators)
- limited `JOIN` (currently, only `INNER JOIN` and `LEFT JOIN` are supported, single foreign column only)
- very limited aggregation: limited `COUNT`, no `GROUP BY` and `HAVING` clauses, no window functions
- no set operations on result sets (currently, only a very limited use of `UNION` is implemented)
- no subqueries
- limited support for binary data (not `BIT` type, no collations and explicit charsets)
- no advanced transaction management (currently, autocommit is forced, but concurrency is handled efficiently)

The following additions are expected in the next major versions:

| Feature | MiniBase version |
| ------- | ---------------: |
| `BIT` type | `0.6.0` |
| Collations and charsets | `0.6.0` |
| Arbitrary WHERE | `0.7.0` |
| Arbitrary JOIN condition | `0.7.0` |
| Subqueries | `0.7.0` |
| CROSS join | `0.7.0` |
| More aggregation functions | `0.8.0` |
| GROUP BY and HAVING | `0.8.0` |
| Window functions | `0.8.0` |
| Set operations (e.g. UNION) | `0.8.0` |

## Keywords

Keywords are case-insensitive reserved words that cannot be used for other purposes without quotation marks.

Here is the current complete list of keywords:

`AND`, `AS`, `ASC`, `AUTOCOMMIT`, `BETWEEN`, `BIGINT`, `BINARY`, `BLOB`, `BOOL`, `BOOLEAN`, `BOTH`, 
`BY`, `CALL`, `CASE`, `CAST`, `CHAR`, `CLOB`, `CONVERT`, `COUNT`, `CURRENT_CATALOG`, 
`CURRENT_DATE`, `CURRENT_SCHEMA`, `CURRENT_TIME`, `CURRENT_TIMESTAMP`, `CURRENT_USER`, `DATABASES`, 
`DATE`, `DATETIME`, `DATETIMEO`, `DATETIMETZ`, `DAY`, `DEC`, `DECIMAL`, `DEFAULT`, `DELETE`, 
`DESC`, `DISTINCT`, `DIV`, `DOUBLE`, `ELSE`, `END`, `ESCAPE`, `EXTRACT`, `FALSE`, `FETCH`, `FIRST`, 
`FLOAT`, `FOR`, `FROM`, `HOUR`, `IDENTITY`, `ILIKE`, `IN`, `INNER`, `INSERT`, `INSTANT`, `INT`, 
`INTEGER`, `INTERVAL`, `INTO`, `IS`, `JOIN`, `LAST`, `LAST_INSERT_ID`, `LEADING`, `LEFT`, `LIKE`, 
`LIMIT`, `MINUTE`, `MOD`, `MONTH`, `NCHAR`, `NEXT`, `NOT`, `NULL`, `NULLS`, `NUMERIC`, `NVARCHAR`, 
`OFFSET`, `ON`, `ONLY`, `OR`, `ORDER`, `OUTER`, `OVERLAPS`, `POSITION`, `PRECISION`, `READONLY`, 
`REAL`, `REGEXP`, `REPLACE`, `RIGHT`, `RLIKE`, `ROW`, `ROWS`, `SCHEMAS`, `SECOND`, `SELECT`, 
`SESSION_USER`, `SET`, `SHOW`, `SMALLINT`, `SUBSTR`, `SUBSTRING`, `SYSTEM_USER`, `TABLES`, `TEXT`, 
`THEN`, `TIME`, `TIMEO`, `TIMESTAMP`, `TIMESTAMPO`, `TIMESTAMPTZ`, `TIMETZ`, `TIMEZONE`, 
`TIMEZONE_HOUR`, `TIMEZONE_MINUTE`, `TINYINT`, `TO`, `TRAILING`, `TRIM`, `TRUE`, `UNION`, `UNIT`, 
`UNKNOWN`, `UPDATE`, `USE`, `UTCOFFSET`, `VALUES`, `VARBINARY`, `VARCHAR`, `WHEN`, `WHERE`, `WITH`, 
`WITHOUT`, `XOR`, `YEAR`, `ZONE`,

## Identifiers

An identifier can be an arbitrary string.
If it's not a keyword and also matching to the regular expression `(?!\d)\w+`
(or more precisely: `[\p{L}_][\p{N}\p{L}_]*`)
then you can write it in a bare form, like in this query:

```sql
SELECT some_column FROM some_table;
```

Any other identifier must be written between `` ` `` or `"` (you can escape by doubling):

```sql
SELECT `1col` AS `from`, "2col" AS `2``col` FROM `table`
```

In some cases, identifiers can also form a hierarchy, for example:

- `some_schema.some_table`
- `some_table.some_column`
- `some_schema.some_table.some_column`
- `` `schema`.`table`.`column` ``
- `` `sche.ma`.`table` ``

The hierarchical syntax can be used wherever a table or column name can be written.

## Aliases

Tables and columns can be aliased in queries.

You can alias an identifier using one of the two variants:

- without `AS`, e. g. `col c`, `` `sch`.`tbl` t `` etc.
- with `AS`, e. g. `col AS c`, `` `sch`.`tbl` AS t `` etc.

## Literals

Currently, five types of literals are supported: integer, decimal, boolean, string, and null.

### Integer literals

Arbitrarily large unsigned integer literals are supported (e.g. `0`, `432345`, `72016234137468237434`).
You can use the unary sign operators for negative or explicitly positive values (e.g. `-123`, `+2`).
Note that both unsigned and signed literals result in a signed type (so the result of `123 - 321` is `-198`).

### Decimal literals

An unsigned decimal literal contains a dot and at least one digit (e.g. `1.43`, `.023`, `45.`).
You can use the unary sign operators for negative or explicitly positive values (e.g. `-.004`).
Decimal numbers remember to their scale, so `0.0400` and `0.04` are numerically equal but printed differently.

### Boolean literals

There are two possible boolean values, for each there is a literal keyword: `TRUE` and `FALSE`.
Additionally, there is `UNKNOWN` (traditionally used for unknown boolean values), but it's actually an alias of `NULL`.

### String literals

Simple string literals are written between single quotes (e.g. `'some text'`).
If the text itself contains a single quote, it must be doubled (e.g. `'it''s possible'`).

You can write multiple simple string literals consecutively, and they will be joined.
This method can be used for breaking the string to multiple lines, e.g.:

```sql
SELECT
    'this is line 1 '
    'this is line 2 '
    'this is line 3 '
;
```

Escaped string literals are introduced with the `e` prefix (e.g. `e'a\tb'`).
Behavior of escaped string literals is similar to Postgres'.
You can use the following kinds of escape sequences:

| Name | Example |
| ---- | ------- |
| Backspace | `\b` |
| Horizontal tabulator | `\t` |
| Line feed | `\n` |
| Form feed | `\f` |
| Carriage return | `\r` |
| 8-bit hexadecimal codepoint | `\xA`, `\x1E` |
| 16-bit hexadecimal codepoint | `\u003D` |
| 32-bit hexadecimal codepoint | `\U0001F986` |
| Octal codepoint | `\2`, `\04` |

Any other subsequent character be resolved to itself, including `'` and `\` (so, e.g. `'a\'b''c\\d'` means `a'b'c\d`).

In case multiple/multi-line escape literals only the first one must be prefixed with `e`:

```sql
SELECT
    e'escaped \t line 1\n'
    'escaped \t line 2\n'
    'escaped \t line 3\n'
;
```

### The `NULL` literal

Null values can be explicitly created using the `NULL` keyword.

| Literal type | Examples | Description |
| ------------ | ------- | ----------- |
| Integer | `84337892`, `-4329` | Arbitrarily large whole number |
| Decimal | `35.23`, `.0127`, `-0.5` | Decimal number with scale |
| String | `'some text'`, `'couldn\'t'`, `'a\\b\\c'` | Decimal number with scale |

- Integer literals, e. g. `0`, `123`, `-432345`, `16234137468237434`
- String literals, e. g. `'some text'`, `'couldn\'t'`, `'a\\b\\c'`

## Types and cast

TODO

## Expressions

Expressions are possibly compound syntactic structures evaluating to a single value.
There are three major categories of them, see below.

### Operators

These are the supported operators in precedence order (higher precedence first):

| Name | Symbol | Example | Result |
| ---- | :----: | :-----: | :----: |
| Unary minus | `-` | `- 3` | `-3` |
| Unary plus | `+` | `+5` | `5` |
| Logical NOT | `NOT` | `NOT TRUE` | `FALSE` |
| Inline cast | `::` | `'12'::int` | `12` |
| Multiplication | `*` | `2 * 3` | `6` |
| Modulo | `MOD` | `-7 MOD 5` | `3` |
| Remainder | `%` | `-7 % 5` | `-2` |
| Integer division | `/` (or `DIV`) | `7 / 2` | `3` |
| Real division | `/` (or `DIV`) | `7.0 / 2` | `3.5` |
| Addition | `+` | `4 + 7` | `11` |
| Subtraction | `-` | `4 - 7` | `-3` |
| Logical AND | `AND` | `TRUE AND FALSE` | `FALSE` |
| Logical XOR | `XOR` | `TRUE XOR FALSE` | `TRUE` |
| Logical OR | `OR` | `TRUE OR FALSE` | `TRUE` |
| Less than | `<` | `2 < 5` | `TRUE` |
| Less than or equal to | `<=` | `2 <= 5` | `TRUE` |
| Greater than | `>` | `2 > 5` | `FALSE` |
| Greater than or equal to | `>=` | `2 >= 5` | `FALSE` |
| Equal to | `=` | `2 = 5` | `FALSE` |
| Different from | `!=` OR `<>` | `2 != 5` | `TRUE` |
| Inline concatenation | `||` | `'Value: ' || 3` | `'Value: 3'` |

Currently, division-by-zero behavior is lenient,
so a zero operand after a division operator (`/`, `DIV`, or `%`) will result in `NULL`.

In most cases, each of these returns with `NULL` if any of the given operands is `NULL`.
The only exception is when a logical expression can be evaluated based on the non-null operand,
for example `NULL OR TRUE` is evaluated to `TRUE`.

You can use parentheses to enforce a different execution order, for example:

```
NOT (3 > 5 OR NOT ('1' || '2')::int > 7)
```

### Special expressions

Several special expressions are supported, taken from the standard, as well as vendors.

### The `BETWEEN` expression

The expression `x BETWEEN a AND b` is equivalent to this compound expression:

```
x >= a AND x <= b
```

The expression `x NOT BETWEEN a AND b` is equivalent to `NOT(x BETWEEN a AND b)`.

Here are some examples of how it is evaluated:

| Example | Result |
| ------- | ------ |
| `3 BETWEEN 1 AND 5` | `TRUE` |
| `3 BETWEEN 5 AND 1` | `FALSE` |
| `3 BETWEEN 3 AND 3` | `TRUE` |
| `3 BETWEEN 3 AND 1` | `FALSE` |
| `3 BETWEEN 1 AND NULL` | `NULL` |
| `3 BETWEEN 5 AND NULL` | `FALSE` |
| `3 BETWEEN NULL AND 5` | `NULL` |
| `3 BETWEEN NULL AND 1` | `FALSE` |
| `3 NOT BETWEEN 1 AND 5` | `FALSE` |
| `3 NOT BETWEEN 1 AND NULL` | `NULL` |

### The `IN` expression

The expression `x IN (a1, a2, a3)` checks if `x` is equal to at least one in the list.
It returns with `TRUE` if `x` is not null and found,
`FALSE` if neither `x` or the listed values are null and not found,
and finally `NULL` if `x` is null or there is a null value in the list while `x` not found.
The expression `x NOT IN (a1, a2, a3)` is equivalent to `NOT(x IN (a1, a2, a3))`.

Some examples of how it is evaluated:

| Example | Result |
| ------- | ------ |
| `1 IN (1)` | `TRUE` |
| `2 IN (1, 2, 3)` | `TRUE` |
| `3 IN (1, 2, 4)` | `FALSE` |
| `1 IN (1, NULL, 3)` | `TRUE` |
| `2 IN (1, NULL, 3)` | `NULL` |
| `NULL IN (1, 2, 3)` | `NULL` |
| `NULL IN (1, NULL, 3)` | `NULL` |
| `1 NOT IN (1)` | `FALSE` |
| `NULL NOT IN (1, 2, 3)` | `NULL` |

### The `IS NULL` expression

The expression `x IS NULL` checks if `x` is null.
The expression `x IS NOT NULL` is equivalent to `NOT(x IS NULL)`.

Some examples:

| Example | Result |
| ------- | ------ |
| `1 IS NULL` | `FALSE` |
| `NULL IS NULL` | `TRUE` |
| `1 IS NOT NULL` | `TRUE` |
| `NULL IS NOT NULL` | `FALSE` |

### The `LIKE` expression

The expression `x LIKE p` checks if `x` matches to the SQL like pattern `p`.
The optional `ESCAPE` clause specifies an escape character.
The expression `x NOT LIKE p` is equivalent to `NOT(x LIKE p)`.
Instead of `LIKE`, the `ILIKE` keyword can also be used, in which case the pattern matching will be case-insensitive.

There are two special wildcards:

- `_`: matches to any single character
- `%`: matches to any sequences of characters, including empty

Both can be escaped by preceding it with the escape character (if specified).

Some examples:

| Example | Result |
| ------- | ------ |
| `'lorem' LIKE 'lor'` | `FALSE` |
| `'lorem' LIKE 'lor%'` | `TRUE` |
| `'lorem' LIKE 'lo_e%'` | `TRUE` |
| `'lorem' LIKE 'LO_E%'` | `FALSE` |
| `'lorem' ILIKE 'LO_E%'` | `TRUE` |
| `'lorem' NOT LIKE 'LO_E%'` | `TRUE` |
| `'lorem' NOT ILIKE 'LO_E%'` | `FALSE` |

If any specified parameter is null then null will be returned.

### The `REGEXP` expression

The `REGEXP` expression is similar to `LIKE` but the pattern is interpreted as a Java regular expression,
and the `ESCAPE` clause is not supported.
The alternative keyword `RLIKE` can also be used in place of `REGEXP`.

| Example | Result |
| ------- | ------ |
| `'ipsum' REGEXP '^i'` | `TRUE` |
| `'ipsum' RLIKE '^i'` | `TRUE` |
| `'lorem' REGEXP 'x$'` | `FALSE` |
| `NULL REGEXP '.'` | `NULL` |
| `'lorem' REGEXP NULL` | `NULL` |

### The `OVERLAPS` expression

In MiniBase, `OVERLAPS` works with any type, not only with temporal values.

The expression `(x, y) OVERLAPS (a, b)` checks if the interval `x` to `y`
has an intersection with the interval `a` to `b`.
The intervals are commutative, the lesser value is inclusive, and the greater value is exclusive.
If the two values of an interval is equal, then the interval will be a point containing this single value.
If none of the operands is null then the result is non-null.
If there is a single null value, the result can still be `TRUE` if there is an overlap regardless of the missing value.

Some examples of how it is evaluated:

| Example | Result |
| ------- | ------ |
| `(2, 5) OVERLAPS (3, 6)` | `TRUE` |
| `(2, 5) OVERLAPS (2, 2)` | `TRUE` |
| `(1, 5) OVERLAPS (2, 3)` | `TRUE` |
| `(2, 5) OVERLAPS (5, 7)` | `FALSE` |
| `(2, 5) OVERLAPS (5, 5)` | `FALSE` |
| `(5, 2) OVERLAPS (2, 2)` | `TRUE` |
| `(1, 5) OVERLAPS (NULL, 3)` | `TRUE` |
| `(1, 5) OVERLAPS (NULL, 7)` | `NULL` |
| `(NULL, NULL) OVERLAPS (NULL, NULL)` | `NULL` |

### The `CASE` expression

The simple `CASE` expression accepts a condition value and contains one ore more `WHEN` clauses,
branching based on the accepted value.
It optionally contains an `ELSE` clause:

```sql
CASE x
  WHEN 1 THEN 'one'
  WHEN 2 THEN 'two'
  WHEN 3 THEN 'three'
  ELSE 'other'
END
```

The search `CASE` expression is similar, but there is no condition value.
Instead, the branches will be interpreted directly as boolean:

```sql
CASE
  WHEN @var = 'lorem' THEN 'LOREM'
  WHEN @var = 'ispum' THEN 'IPSUM'
  ELSE 'OTHER'
END
```

The result type and nullability is calculated accordingly.

### Regular functions

These are the supported deterministic regular functions:

| Name | Description | Example | Result |
| ---- | ------- | ------ | ----- |
| `ABS` | Absolute value | `ABS(-3)` | `3` |
| `ASCII` | Codepoint of character | `ASCII('a')` | `97` |
| `ATAN2` | Arctangent of ratio | `ATAN2(1, 2)` | `0.46364760900081` |
| `BIT_LENGTH` | Length in bits | `BIT_LENGTH('tűzőgép')` | `80` |
| `CHAR_LENGTH` | Length in characters | `CHAR_LENGTH('tűzőgép')` | `7` |
| `CHARACTER_LENGTH` | Alias for `CHAR_LENGTH` |  |  |
| `CHR` | Character of codepoint | `CHR(65)` | `'A'` |
| `COALESCE` | Finding first non-null | `COALESCE(NULL, 1, 2)` | `1` |
| `CONCAT` | String concatenation | `CONCAT('lorem', 'ipsum')` | `'loremipsum'` |
| `CONCAT_WS` | Null-tolerant string concatenation with separator | `CONCAT_WS(',', 'a', NULL, 'b', 'c')` | `'a,b,c'` |
| `DECODE` | Decoding binary data using the choosen algorithm | `DECODE('bG9yZW0=', 'base64')` | `'lorem'` |
| `ENCODE` | Encoding to binary data using the choosen algorithm | `ENCODE('lorem', 'base64')` | `'bG9yZW0='` |
| `GCD` | Greatest common divisor | `GCD(12, 27)` | `3` |
| `GREATEST` | Greatest of the given values | `GREATEST(3, 7, NULL, -11)` | `7` |
| `INITCAP` | Title-case string | `INITCAP('lorem IPSUM dOlOr')` | `'Lorem Ipsum Dolor'` |
| `LCM` | Least common multiple | `LCM(8, 18)` | `72` |
| `LEAST` | Least of the given values | `LEAST(3, 7, NULL, -11)` | `-11` |
| `LEFT` | Left part of string | `LEFT('lorem', 3)` | `'lor'` |
| `LENGTH` | Type-specific length | `LENGTH('tűzőgép')` | `7` |
| `LOG` | Logarithm with specific base | `LOG(2, 8)` | `3.0` |
| `LOWER` | Lowercase string | `LOWER('LoReM')` | 'lorem' |
| `LPAD` | Left-padded string | `LPAD(12, 5, '0')` | `00012` |
| `NULLIF` | Returns with the first value ornull if not equal to the second | `NULLIF('lorem', 'lorem')` | `NULL` |
| `OCTET_LENGTH` | Length in bytes | `OCTET_LENGTH('tűzőgép')` | `10` |
| `ORD` | Codepoint of character | `ORD('ű')` | `369` |
| `PI` | The pi constant | `PI()` | `3.14159265358979` |
| `POW` | Exponentiation | `POW(2, 3)` | `8.0` |
| `POWER` | Alias for `POW` |  |  |
| `REGEXP_REPLACE` | Replace by regular expression | `REGEXP_REPLACE('loREm', '[oe]', 'a', 'gi')` | `'laRam'` |
| `REPEAT` | Repeated string | `REPEAT('ab', 3)` | `'ababab'` |
| `REPLACE` | Replace by string | `REPLACE('lorem ipsum dolor', 'or', 'ith')` | `'lithem ipsum dolith'` |
| `REVERSE` | Reversed string | `REVERSE('lorem')` | `'merol'` |
| `RIGHT` | Right part of string | `RIGHT('lorem', 3)` | `'rem'` |
| `RPAD` | Right-padded string | `RPAD('lorem', 10, '=#:')` | `'lorem=#:=#'` |
| `RRPAD` | Right-padded string, improved | `RRPAD('lorem', 10, '=#:')` | `'lorem#:=#:'` |
| `ROUND` | Rounded numeric value | `ROUND(4.23)` | `4` |
| `SHA256` | SHA256 checksum | `SHA256('lorem')` | `'3400bb495c3f8c4c3483a44c`[...]`'` |
| `SIGN` | Signum | `SIGN(-23)` | `-1` |
| `SPLIT_PART` | Extract part by separator | `SPLIT_PART('lorem,ipsum,dolor', ',', 2)` | `'ipsum'` |
| `TRANSLATE` | Replace characters | `TRANSLATE('lorem', 'mow', 'nöx')` | `'lören'` |
| `UPPER` | Uppercase string | `UPPER('LoReM')` | 'LOREM' |

Some functions are volatile (can return differently when you call again):

| Function | Description |
| -------- | ----------- |
| `NOW()` | gets the current instant |
| `RANDOM()` | generates a random floating point number between 0 (inclusive) and 1 (exclusive) |

Regular function names are identifiers, so you can write them like `` `UPPER`('lorem')`` or `"UPPER"('lorem')` too.

## System functions

There are several global functions that provide system-specific information.
You can use these as magic constants too, omitting the parentheses (e.g. `SELECT CURRENT_TIMESTAMP`).
Here are a list of them:

| Function | Description |
| -------- | ----------- |
| `AUTOCOMMIT()` | checks whether autocommit is on (currently always `TRUE`) |
| `CURRENT_CATALOG()` | alias for `CURRENT_SCHEMA()` |
| `CURRENT_DATE()` | gets the current local date |
| `CURRENT_SCHEMA()` | gets the current selected schema (or `NULL` if not selected) |
| `CURRENT_TIME()` | gets the current zoned time |
| `CURRENT_TIMESTAMP()` | gets the current instant (same as `NOW()`) |
| `CURRENT_USER()` | gets the current database user (currently always empty) |
| `IDENTITY()` | alias for `CURRENT_USER()` |
| `LAST_INSERT_ID()` | gets the last inserted value for an auto-incremented column (or `NULL` if none) |
| `READONLY()` | checks whether the database is read-only or not |
| `SESSION_USER()` | alias for `CURRENT_USER()` |
| `SYSTEM_USER()` | alias for `CURRENT_USER()` |

These functions are keywords and not identifiers, so you must write them in bare form.

### Standard function-like expressions

Some expressions are similar to functions but have a special syntax for the parameters.
Like system functions, their names are keywords.

### The `INTERVAL` expression

While `INTERVAL` is also a type name, it can also be used for introducing interval literals.
The simple `INTERVAL` expression has a single parameter, which can be of multiple types:

- a number or numeric string means seconds, optionally with fractional part
- ISO 8601 duration (e.g. `'P2Y-1DT3H2M'`)
- SQL standard interval (e.g. `3-2 5 12:30`)
- PostgreSQL style verbose interval (e.g. `2 years 1 month 4 hours ago`)

| Expression | Meaning |
| ---------- | ------- |
| `INTERVAL 0` | zero-length interval |
| `INTERVAL 30` | 30 seconds |
| `INTERVAL 7200` | 2 hours |
| `INTERVAL 0.03` | 30 milliseconds |
| `INTERVAL 0.000023047` | 23 047 nanoseconds |
| `INTERVAL '17.12'` | 17 seconds 120 milliseconds |
| `INTERVAL 'P2Y-1DT3H2M'` | 2 years minus 1 days, plus 3 hours and 2 minutes |
| `INTERVAL '10:32:01.5'` | 10 hours 32 minutes 1 second 500 milliseconds |
| `INTERVAL '1-2 3'` | 1 year 2 months 3 days |
| `INTERVAL '1-0 0 00:00:01'` | 1 year and 1 second |
| `INTERVAL '5 days ago` | 5 days ago |
| `INTERVAL '-3 days` | 3 days ago |
| `INTERVAL '2 days -1 hour ago` | 1 hour short of 2 days |

In the PostgreSQL style verbose interval format the unit words are case-insensitive.
These are the supported unit words:

| Unit word | Alternative forms |
| --------- | ----------------- |
| `nanosecond` | `nanoseconds`, `nanos` |
| `microsecond` | `microseconds`, `micros` |
| `millisecond` | `milliseconds`, `millis` |
| `second` | `seconds` |
| `minute` | `minutes` |
| `hour` | `hours` |
| `day` | `days` |
| `week` | `weeks` |
| `month` | `months` |
| `year` | `years` |
| `decade` | `decades` |
| `century` | `centurys`, `centuries` |
| `millennium` | `millenniums`, `millennia` |
| `era` | `eras` |

The simple `INTERVAL` expression is equivalent to the `INTERVAL(s)` converter method.

Beyond the simple form, there is an optional second parameter: the time unit qualifier.
If it's specified the meaning of the first parameter changes:

- number or numeric string: quantity in the specified unit (for any other unit than seconds the fractional part will be erased)
- string in `12:34` format plus `SECOND` format: minutes and seconds
- other case with string input: interpret as in the simple case then erase accordingly

Some examples with unit:

| Expression | Meaning |
| ---------- | ------- |
| `INTERVAL 0 HOUR` | zero-length interval |
| `INTERVAL 4 MINUTE` | 4 minutes |
| `INTERVAL 7.5 SECOND` | 7 second 500 millis |
| `INTERVAL 7.5 DAY` | 7 days |
| `INTERVAL '12:30' SECOND` | 12 minutes 30 seconds |
| `INTERVAL '12:30' MINUTE` | 12 hours 30 minutes |
| `INTERVAL '12:30' HOUR` | 12 hours |

The same set of unit names are supported as for the `INTERVAL` type specification.
Unit parameters and the `TO` syntax is also supported in the same way
(e.g. `INTERVAL '1:23:42.1' HOUR(2) TO SECOND(1)`).

### The `TRIM` expression

The expression `TRIM(x)` or `TRIM(FROM x)` removes all leading and trailing spaces from `x` (interpreted as a string).
The expression `TRIM(c FROM x)` removes all leading and trailing occurences of the first character of `c`
(if `c` is an empty string then it will return with `x` unchanged).

Some examples:

| Expression | Result |
| ---------- | ------ |
| `TRIM('')` | `''` |
| `TRIM('     ')` | `''` |
| `TRIM('   lorem')` | `'lorem'` |
| `TRIM(FROM '   lorem')` | `'lorem'` |
| `TRIM('m' FROM '   lorem')` | `'   lore'` |

If any specified parameter is null then null will be returned.

### The `SUBSTRING` expression

The `SUBSTRING` (or `SUBSTR`) expression extracts a substring from an input interpreted as string.
It contains at least one of the `FROM` clause and the `FOR` clause.
The `FROM` clause specifies the starting position of the substring,
while the `FOR` clause specifies the length.
Both are 1-indexed, following the standard.
The substring is allowed partially or entirely out of bounds either in the negative or positive (or both) direction.

Some examples:

| Expression | Result |
| ---------- | ------ |
| `SUBSTRING('lorem' FROM 2)` | `'orem'` |
| `SUBSTRING('lorem' FOR 3)` | `'lor'` |
| `SUBSTRING('lorem' FROM 3 FOR 2)` | `'re'` |
| `SUBSTRING('lorem' FROM -4)` | `'lorem'` |
| `SUBSTRING('lorem' FOR -2)` | `''` |
| `SUBSTRING('lorem' FROM -1 FOR 4)` | `'lo'` |
| `SUBSTRING('lorem' FROM -5 FOR 2)` | `''` |
| `SUBSTRING('lorem' FROM -1 FOR 12)` | `'lorem'` |

If any specified parameter is null then null will be returned.

### The `POSITION` expression

The expression `POSITION(x IN a)` searches for the first occurence position of `x` in `a` (both interpreted as a string).
The resulting position is 1-indexed.
If no occurence is found then `0` will be returned.

Some examples:

| Expression | Result |
| ---------- | ------ |
| `POSITION('x' in 'lorem')` | `0` |
| `POSITION('r' in 'lorem')` | `3` |
| `POSITION('ore' in 'lorem')` | `2` |
| `POSITION('lorem' in 'ore')` | `0` |

If any specified parameter is null then null will be returned.

### The `EXTRACT` expression

The expression `EXTRACT(f FROM x)` extract the field `f` from `x` (interpreted as a temporal or interval value).

Some examples:

| Expression | Result |
| ---------- | ------ |
| `EXTRACT(MONTH FROM INTERVAL '1 year 2 months 2 days 3 hours')` | `2` |
| `EXTRACT(MINUTE FROM TIME('12:30:03'))` | `30` |
| `EXTRACT(TIMEZONE_HOUR FROM TIMEO('12:45:00-04:00'))` | `-4` |
| `EXTRACT(TIMEZONE_MINUTE FROM TIMEO('12:45:00-01:30'))` | `-30` |
| `EXTRACT(DAY FROM NULL)` | `NULL` |

These are the supported extraction fields:

`YEAR`, `MONTH`, `DAY`, `HOUR`, `MINUTE`, `SECOND`, `TIMEZONE_HOUR`, `TIMEZONE_MINUTE`

### The `CAST` expression

The expression `CAST(x AS t)` converts `x` to the type `t`.

Some examples:

| Expression | Result |
| ---------- | ------ |
| `CAST('12' AS INT)` | `12` |
| `CAST(34.2 AS INT)` | `34` |
| `CAST('lorem' AS INT)` | `0` |
| `CAST(43.257 AS DECIMAL(4, 2))` | `43.25` |
| `CAST('lorem' AS CHAR(3))` | `'lor'` |
| `CAST('lorem' AS NULL)` | `NULL` |
| `CAST(NULL AS CHAR(3))` | `NULL` |

For more information, see the section about types and cast.

### The `CONVERT` expression

The expression `CONVERT(x, t)` or `CONVERT(t, x)` converts `x` to the type `t`.
It's equivalent to the corresponding `CAST(x AS t)` expression.

Some examples:

| Expression | Result |
| ---------- | ------ |
| `CONVERT('12', INT)` | `12` |
| `CONVERT(INT, '12')` | `12` |
| `CONVERT('lorem', CHAR(3))` | `'lor'` |

## Select data from tables

You can select records from a table with the `SELECT` statement.

The simplest table select is to query all records and columns:

```sql
SELECT * FROM tbl;
```

You can also specify some columns and/or filters:

```sql
SELECT id, col1, col2 some_alias, col3 AS some_other_alias FROM tbl;
```

Table alias, more wildcards, custom expresssions,
sorting (using the `ORDER BY` clause), limiting (using the `LIMIT` clause) are also supported:

```sql
SELECT CONCAT('#', t.id), t.* FROM tbl t WHERE id > 20 ORDER by id, label DESC NULLS FIRST LIMIT 5;
```

You can select data from multiple tables using joins.
Currently two types of joins are supported: `INNER JOIN` and `LEFT JOIN` (or `LEFT OUTER JOIN`).
A very simple joined select looks like this:

```sql
SELECT t1.id, t2.* FROM table1 t1 LEFT JOIN table2 t2 ON t2.t1_id = t1.id;
```

Finally, here is a complex example including most of the supported features for table select:

```sql
SELECT
  t3.*,
  t1.label,
  t1.created t_created,
  CONCAT(UPPER(@somevar), ': ', t4.col1) AS `concatenated value`,
  LEAST(5, POW(t1.level, 2) + 1) AS `normalized level`,
  CASE t1.level
    WHEN 1 THEN 'one'
    WHEN 2 THEN 'two'
    ELSE 'other'
  END AS `level name`
FROM base_table t1
INNER JOIN inner_joined_table t2 ON t2.id = t1.i_id
LEFT JOIN left_joined_table t3 ON t3.id = t1.l_id
LEFT JOIN other_left_joined_table t4 ON t4.id = t3.l2_id
WHERE
  t1.category = 'basic' AND
  t1.id >= 150 AND
  t1.id < 960 AND
  t4.year BETWEEN 1995 AND 2003 AND
  t4.phone IS NOT NULL
ORDER BY
  t1.level,
  1 DESC,
  t3.price ASC NULLS LAST
LIMIT 10
OFFSET 5
```

## Aggregated queries

Currently there is only a very limited support for count queries.
There is a wildcarded form: `COUNT(*)`, and a field form: `COUNT(x)` (where `x` is a table field).
These can be used in restricted `SELECT` queries that selects only the count field,
and contains only these clauses

- `FROM`, with optional table alias
- `WHERE` (optional)
- `LIMIT` (optional, necessary for some ORMs)

Here is a tipical example of a count query:

```sql
SELECT COUNT(*) FROM some_table WHERE some_column > 10
```

## Other statements

### Switch to schema

You can set the current database schema with the `USE` statement.

Example:

```sql
USE some_schema;
```

### Show schemas and tables

You can list all the schemas:

```sql
SHOW SCHEMAS;
```

Or:

```sql
SHOW DATABASES;
```

You can list all the tables in the current schema:

```sql
SHOW TABLES;
```

Or in a specific schema:

```sql
SHOW TABLES FROM some_schema;
```

All the above can be filtered with the `LIKE` clause, for example:

```sql
SHOW TABLES FROM some_schema LIKE 'a%';
```

### Set variables

You can define user variables for the current session with the `SET` statement:

```sql
SET @some_variable = 'lorem ipsum';
```

You can quote variable names too:

```sql
SET @`some special `` variable name` = 35;
```

### Select variables and other values

You can execute several types of `SELECT` queries without a table.

To show a variable:

```sql
SELECT @some_variable;
```

Optionally you can use a `FROM` clause to the special table `UNIT`:

```sql
SELECT 'value' FROM UNIT;
```

Or to show a special value:

```sql
SELECT CURRENT_SCHEMA();
```

These special values can be selected with alternative syntaxes (just like to other RDMBS engines), for example:

```sql
SHOW CURRENT_SCHEMA;
```

You can select multiple values in a single query:

```sql
SELECT LAST_INSERT_ID() AS id, @some_variable AS var;
```

Or even multiple rows:

```sql
SELECT 1 AS no, @some_variable AS var
  UNION
SELECT 2 AS no, @some_other_variable AS var
  UNION
SELECT 3 AS no, @some_more_variable AS var;
```

### Select table size or count of matching records

You can count rows in a table:

```sql
SELECT COUNT(*) FROM some_table;
```

Or count rows that match a filter:

```sql
SELECT COUNT(*) FROM books WHERE id > 3 AND category IS NOT NULL;
```

### Insert (or replace) records to table

You can insert new records to a table with the `INSERT` statement:

```sql
INSERT INTO tbl VALUES (null, 'lorem', 42);
```

Or with explicitly enumerated columns:

```sql
INSERT INTO tbl (col1, col2) VALUES ('lorem', 42);
```

The `REPLACE` statement is nearly the same, but it deletes all the conflicting records:

```sql
REPLACE INTO tbl (col1, col2) VALUES ('lorem', 42);
```

### Update records in table

You can change existing values in a table with the `UPDATE` statement:

```sql
UPDATE tbl SET col1 = 'lorem', col2 = 42 WHERE id BETWEEN 16 AND 23;
```

The `WHERE` clause is optional, if omitted, all records will be updated.

### Delete records from table

You can delete records from a table with the `DELETE` statement:

```sql
DELETE FROM tbl WHERE id = 44;
```

The `WHERE` clause is optional, if omitted, all records will be deleted.
