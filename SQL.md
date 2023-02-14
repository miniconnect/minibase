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

## Literals

Currently, two types of literals are supported:

- Integer literals, e. g. `0`, `123`, `-432345`, `16234137468237434`
- String literals, e. g. `'some text'`, `'couldn\'t'`, `'a\\b\\c'`

Integer literals can be arbitrarily large.
Use arithmetic operators or string literals to produce fractional and other numbers.
Use string literals for setting dates and other complex data fields.

## Use schema

You can set the current database schema with the `USE` statement.

Example:

```sql
USE some_schema;
```

## Show schemas and tables

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

## Set variables

You can define user variables for the current session with the `SET` statement:

```sql
SET @some_variable = 'lorem ipsum';
```

You can quote variable names too:

```sql
SET @`some special `` variable name` = 35;
```

## Select variables and other values

You can execute several types of `SELECT` queries without a table.

To show a variable:

```sql
SELECT @some_variable;
```

Or to show a special value:

```sql
SELECT CURRENT_SCHEMA();
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

## Select table size or count of matching records

You can count rows in a table:

```sql
SELECT COUNT(*) FROM some_table;
```

Or count rows that match a filter:

```sql
SELECT COUNT(*) FROM books WHERE id > 3 AND category IS NOT NULL;
```

## Select data from tables

TODO

<!-- TODO selectQuery -->

## Insert (or replace) records to table

TODO

<!-- TODO insertQuery -->

## Update records in table

TODO

<!-- TODO updateQuery -->

## Delete records from table

TODO

<!-- TODO deleteQuery -->
