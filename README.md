# MiniBase

RDBMS framework for miniConnect.

## Overview

MiniBase is a flexible java framework for building relational database engines.
It has built-in support for SQL parsing, query execution, storages, indexing, and more.
It manages concurrency, currently a very basic transaction management is implemented.

One of its implementations is [HoloDB, a database seemingly filled with random data](https://github.com/miniconnect/holodb).

## SQL support

[Click here for the full SQL documentation](SQL.md)

MiniBase has built-in support for SQL queries. Features:

- Limited support for the CRUD operations
- Multiple schemas, multi-schema queries
- User variables
- Simple expressions

`SELECT` example, demonstrating most of the supported features:

```sql
SELECT
  l.*,
  t.label,
  t.created t_created,
  CONCAT(@somevar, ': ', l2.col1) AS `concatenated value`
FROM base_table t
INNER JOIN inner_joined_table i ON i.id = t.i_id
LEFT JOIN left_joined_table l ON l.id = t.l_id
LEFT JOIN other_left_joined_table l2 ON l2.id = l.l2_id
WHERE
  t.category = 'basic' AND
  t.id >= 150 AND
  t.id < 960 AND
  l2.year BETWEEN 1995 AND 2003 AND
  l2.phone IS NOT NULL
ORDER BY
  t.level,
  1 DESC,
  l.price ASC NULLS LAST
LIMIT 10
```
