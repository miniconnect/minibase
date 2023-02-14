## MiniBase SQL support

## Overview

[For the ANTLR4 grammar file click here.](projects/query/src/main/antlr/hu/webarticum/minibase/query/query/antlr/grammar/SqlQuery.g4)

MiniBase has a partial support for SQL.
It supports the most important CRUD features, user variables, schema selections and some more.
(A later goal is full SQL92 support.)

Various optimizations are performed when the queries are run,
indexes are intelligently selected, the joined tables will be also rearranged if necessary.

## Keywords

Keywords are case-insensitive reserved words that cannot be used for other purposes without quotation marks.

Some of these keywords, without claiming to be complete:
`SELECT`, `UPDATE`, `DELETE`, `FROM`, `SET`, `SCHEMA`, `AND`.

For a full list of keywords see the grammar file above.

## Identifiers

You can use any non-keyword name not starting with a digit just in its naked form:

```sql
SELECT some_column FROM some_table;
```

Any other name must be written between `` ` `` or `"`:

```sql
SELECT `1col` AS `from`, "2col" AS `2``col` FROM `table`
```

In some cases identifiers can also form a hierarchy, form example:

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

Currently, two types of literals are supported:

- Integer literals, e. g. `0`, `123`, `-432345`, `16234137468237434`
- String literals, e. g. `'some text'`, `'couldn\'t'`, `'a\\b\\c'`

Integer literals can be arbitrarily large.
Use arithmetic operators or string literals to produce fractional and other numbers.
Use string literals for setting dates and other complex data fields.

## Functions and operators

Special global functions:

| Function | Description |
| -------- | ----------- |
| `CURRENT_USER()` | current database user (currently always empty) |
| `CURRENT_SCHEMA()` | current selected schema (or `NULL` if not selected) |
| `CURRENT_CATALOG()` | alias for `CURRENT_SCHEMA()` |
| `READONLY()` | checks whether the database is read-only or not |
| `AUTOCOMMIT()` | checks whether autocommit is on (currently always `1`) |
| `LAST_INSERT_ID()` | gets the last inserted value for an auto-incremented column (or `NULL` if none) |

Expression functions:

| Name | Example | Description |
| ---- | ------- | ----------- |
| `CONCAT` | `CONCAT('hello, ', name)` | Concats values to a longer string (always returns a string, skips `NULL` values) |
| `COALESCE` | `COALESCE(col1, col2, 'fallback value')` | Returns the first non-`NULL` value if any, `NULL` otherwise |

Operators:

| Operator | Example | Result | Description |
| -------- | :-----: | :----: | ----------- |
| `-` | `- 4` | `-4` | Negates a value |
| `+` | `2 + 3` | `5` | Adds two values |
| `-` | `100 - 20` | `80` | Subtracts a value from an other |
| `*` | `2 * 7` | `14` | Multiplies two values |
| `/` | `5 / 2` | `2.5` | Divides a value with an other |
| `DIV` | `13 / 5` | `2` | Divides a value with an other resulting an integer |
| `%` or `MOD` | `13 % 5` | `3` | Get the remainder dividing a value with an other |

## Statements

### Use schema

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

### Set variable

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

### Select data from tables

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
  CONCAT(@somevar, ': ', t4.col1) AS `concatenated value`
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

The `WHERE` clause is optional, if omitted, all records will be updated.
