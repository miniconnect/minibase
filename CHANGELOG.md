# Changelog

## Version 0.2.0

Released on *2023-12-16*

### Fixed:

- Fixed variable auto-conversion
- Fixed NPE in range check
- Fixed limit problem with joins
- Fixed problem with whre filter on left joined table

### Added:

- Support for default values for columns
- Implementation of `isUnique()` in indexes
- More expression types and methods

### Improved:

- Added module definition and utf-8 encoding explicitly
- Extended syntax for `COUNT` queries
- Extended syntax for `LIMIT` clauses
- Many improvements in the bundled query executor

## Version 0.1.0

Released on *2023-01-11*

### Migration:

- Project migrated from the main `miniconnect` repo

### Added:

- Full support for select queries without a table
- Support for range conditions in where clause
- Support for expressions in select queries

### Improved:

- Improved and restructured gradle build

### Fixed:

- Fixed order index lookup
- Fixed value translator creation
- Fixed column check in select expressions
