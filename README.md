# MiniBase

RDBMS framework for miniConnect.

## SQL support

MiniBase has built-in support for SQL queries. Features:

- Simple `UPDATE`, `INSERT`, `REPLACE`, and `DELETE`
- Somewhat complex `SELECT` queries
- Multiple schemas, multi-schema queries
- User variables
- Simple expressions

`SELECT` example, demonstrating most of the available features:

```sql
SELECT
  l.*,
  t.label,
  t.created t_created,
  CONCAT(@somevar, ': ', l2.col1) AS `concatenated value`,
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
