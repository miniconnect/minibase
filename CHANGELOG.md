# Changelog


## Version 0.5.0

Released on *2026-03-03*

### Fixed:

- Fixed several query parsing issues
- Fixed several nullability issues
- Added normalization of unquoted identifiers
- Fixed issues with parallel query execution
- Fixed a sorting problem in `DiffTable`

### Added:

- Added many new functions and expressions
- Added date/time/interval types and sematics

### Improved:

- Improved conversion and unnification rules between types
- Improved the type system
- Improved literal support (e.g. scientific notation)

### Development:

- Updated to gradle 9 and improved build
- Introduced the query test framework


## Version 0.4.0

Released on *2025-09-29*

### Fixed:

- Fixed handling of lastInsertedId

### Added:

- Added support for alias for COUNT
- Added more query capabilities for better ORM integration

### Improved:

- Improved table alias handling in query grammar

### Development:

- Improved project tools


## Version 0.3.0

Released on *2025-03-15*

### Added:

- Added `DynamicStorageEngine`

### Improved:

- Improved logging

### Development:

- Upgrade to gradle 8
- Simplified ANTLR generation


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
