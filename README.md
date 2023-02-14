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
