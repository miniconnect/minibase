grammar SqlQuery;

@header {
package hu.webarticum.minibase.query.query.antlr.grammar;
}

file: sqlQuery EOF;

sqlQuery
    : selectCountQuery
    | selectQuery
    | standaloneSelectQuery
    | showSpecialQuery
    | updateQuery
    | insertQuery
    | deleteQuery
    | showSchemasQuery
    | showTablesQuery
    | useQuery
    | setVariableQuery
    ;

selectCountQuery: (
    SELECT COUNT S_PAR_START ( wildcardSelectItem | scopeableFieldName ) S_PAR_END fieldAliasPart=aliasPart?
    FROM ( schemaName S_DOT )? tableName tableAliasPart=aliasPart?
    wherePart?
    limitPart?
);

selectQuery: (
    SELECT selectPart
    FROM ( schemaName S_DOT )? tableName tableAliasPart=aliasPart?
    joinPart*
    wherePart?
    orderByPart?
    offsetLimitPart?
);

joinPart: (
	( innerJoin | leftJoin )
	( targetSchemaName=schemaName S_DOT )? targetTableName=tableName tableAliasPart=aliasPart?
	ON scope1=tableName S_DOT field1=fieldName S_EQ scope2=tableName S_DOT field2=fieldName
);
innerJoin: INNER? JOIN;
leftJoin: LEFT OUTER? JOIN;

selectPart: selectItem ( S_COMMA selectItem )*;
selectItem: aliasableExpression | wildcardSelectItem;
wildcardSelectItem: ( tableName S_DOT )? S_ASTERISK;
offsetLimitPart: offsetPart limitPart?  | limitPart offsetPart? | commaLimitPart;
offsetPart: OFFSET limitParameter ( ROW | ROWS )?;
limitPart: ( LIMIT | FETCH ( FIRST | NEXT ) ) limitParameter ( ( ROW | ROWS ) ONLY? )?;
commaLimitPart: LIMIT offsetValue=limitParameter S_COMMA limitValue=limitParameter;
limitParameter: D_INTEGER | stringLiteral | variable;

standaloneSelectQuery: standaloneSelectRow ( UNION standaloneSelectRow )*;
standaloneSelectRow: SELECT aliasableExpression ( S_COMMA aliasableExpression )* ( FROM UNIT )?;

showSpecialQuery: ( SHOW | CALL ) specialSelectable aliasPart?;

updateQuery: UPDATE ( schemaName S_DOT )? tableName updatePart wherePart?;
updatePart: SET updateItem ( S_COMMA updateItem )*;
updateItem: fieldName S_EQ extendedValue;

insertQuery: ( INSERT | REPLACE ) INTO ( schemaName S_DOT )? tableName fieldList? VALUES insertValueList;
fieldList: S_PAR_START fieldName ( S_COMMA fieldName )* S_PAR_END;
insertValueList: S_PAR_START insertValue ( S_COMMA insertValue )* S_PAR_END;
insertValue: extendedValue | DEFAULT;

deleteQuery: DELETE FROM ( schemaName S_DOT )? tableName wherePart?;

showSchemasQuery: SHOW ( SCHEMAS | DATABASES ) likePart?;

showTablesQuery: SHOW TABLES ( FROM schemaName )? likePart?;

useQuery: USE schemaName;

setVariableQuery: SET variable S_EQ extendedValue;

wherePart: WHERE ( whereItem ( AND whereItem )* | S_PAR_START whereItem ( AND whereItem )* S_PAR_END );
whereItem: scopeableFieldName postfixCondition | S_PAR_START whereItem S_PAR_END;
postfixCondition: simpleRelation extendedValue | betweenRelation | isNull | isNotNull;
simpleRelation: S_EQ | S_LESS | S_LEQ | S_GREATER| S_GEQ;
betweenRelation: BETWEEN firstValue=extendedValue AND secondValue=extendedValue;
isNull: IS ( NULL | UNKNOWN );
isNotNull: IS NOT ( NULL | UNKNOWN );
orderByPart: ORDER BY orderByItem ( S_COMMA orderByItem )*;
orderByItem: ( scopeableFieldName | orderByPosition ) ( ASC | DESC )? ( nullsFirst | nullsLast )?;
nullsFirst: NULLS FIRST;
nullsLast: NULLS LAST;
orderByPosition: D_INTEGER;
aliasableExpression: expression aliasPart?;
aliasPart: AS? alias=identifier;

expression
    : subject=expression S_DOUBLE_COLON typeConstruct
    | left=expression S_ET right=expression
    | left=expression S_SHIFT_LEFT right=expression
    | left=expression S_SHIFT_RIGHT right=expression
    | left=expression S_PIPE right=expression
    | left=expression S_HASH right=expression
    | left=expression ( S_ASTERISK | MOD | S_PERCENT | DIV | S_SLASH ) right=expression
    | left=expression ( S_PLUS | S_MINUS ) right=expression
    | left=expression AND right=expression
    | left=expression XOR right=expression
    | left=expression OR right=expression
    | left=expression ( S_LESS | S_LEQ | S_GREATER | S_GEQ ) right=expression
    | left=expression ( S_EQ | S_NEQ_ANG | S_NEQ_BANG ) right=expression
    | subject=expression NOT? BETWEEN min=expression AND max=expression
    | left=expression S_DOUBLE_PIPE right=expression
    | subject=expression NOT? IN inValueList
    | context=expression IS NOT? isNullOperator=( NULL | UNKNOWN )
    | context=expression NOT? ( LIKE | ILIKE ) pattern=expression ( ESCAPE escape=expression )?
    | context=expression NOT? ( REGEXP | RLIKE ) pattern=expression
    | prefixableExpression
    ;

prefixableExpression
    : unaryArithmeticExpression
    | bitwiseNotExpression
    | notExpression
    | overlapsExpression
    | caseExpression
    | countExpression
    | intervalExpression
    | trimExpression
    | substringExpression
    | positionExpression
    | extractExpression
    | castExpression
    | atomicExpression
    ;

bitwiseNotExpression: S_TILDE prefixableExpression;
notExpression: NOT prefixableExpression;
overlapsExpression: S_PAR_START start1=expression S_COMMA end1=expression S_PAR_END
    OVERLAPS S_PAR_START start2=expression S_COMMA end2=expression S_PAR_END;
unaryArithmeticExpression: ( S_PLUS | S_MINUS ) prefixableExpression;
inValueList: S_PAR_START expression ( S_COMMA expression )* S_PAR_END;
countExpression: COUNT S_PAR_START DISTINCT? S_ASTERISK S_PAR_END |
    COUNT S_PAR_START DISTINCT subExpression=expression S_PAR_END;
caseExpression: CASE (subject=expression)? whenPart+ elsePart? END;
whenPart: WHEN condition=expression THEN result=expression;
elsePart: ELSE expression;
intervalExpression: INTERVAL ( integerLiteral | decimalLiteral | stringLiteral ) intervalSpecifier?;
trimExpression: TRIM S_PAR_START trimSpecification? chars=expression? FROM subject=expression S_PAR_END;
trimSpecification: LEADING | TRAILING | BOTH;
substringExpression: ( SUBSTRING | SUBSTR ) S_PAR_START context=expression
    ( FROM from=expression ( FOR for=expression )? | FOR for=expression ) S_PAR_END;
positionExpression: POSITION S_PAR_START subject=expression IN context=expression S_PAR_END;
extractExpression: EXTRACT S_PAR_START extractFieldName FROM context=expression S_PAR_END;
extractFieldName: YEAR | MONTH | DAY | HOUR | MINUTE | SECOND | TIMEZONE_HOUR | TIMEZONE_MINUTE;
castExpression:
    CAST S_PAR_START expression AS typeConstruct S_PAR_END |
    CONVERT S_PAR_START expression S_COMMA typeConstruct S_PAR_END |
    CONVERT S_PAR_START typeConstruct S_COMMA expression S_PAR_END;
typeConstruct: simpleTypeConstruct | intervalTypeConstruct;
simpleTypeConstruct: typeName ( S_PAR_START ( size=sizeParameter ( S_COMMA scale=sizeParameter )? )? S_PAR_END )?;
intervalTypeConstruct: INTERVAL intervalSpecifier;
sizeParameter: D_INTEGER | stringLiteral;

atomicExpression
    : literal
    | variable
    | specialSelectable
    | scopeableFieldName
    | functionCall
    | S_PAR_START paredExpression=expression S_PAR_END
    ;

specialSelectable: specialSelectableName ( parentheses )?;

specialSelectableName
    : SYSTEM_USER
    | SESSION_USER
    | CURRENT_USER
    | CURRENT_SCHEMA
    | CURRENT_CATALOG
    | CURRENT_DATE
    | CURRENT_TIME
    | CURRENT_TIMESTAMP
    | READONLY
    | AUTOCOMMIT
    | IDENTITY
    | LAST_INSERT_ID
    ;

functionCall: functionName S_PAR_START ( expression ( S_COMMA expression )* )? S_PAR_END;
functionName: identifier | functionNameToken;
functionNameToken: LEFT | RIGHT | TRIM | SUBSTRING | SUBSTR | REPLACE | typeName;

typeName
    : NULL | BOOL | BOOLEAN | BIT | INTEGER | BIGINT | DEC | DECIMAL | FLOAT
    | NVARCHAR | CLOB | BINARY | VARBINARY | BYTEA | BLOB | DATE
    | ( TIME | DATETIME | TIMESTAMP ) ( ( WITH | WITHOUT ) ( TIME ZONE | OFFSET | UTCOFFSET ) )?
    | TIMETZ | DATETIMETZ | TIMESTAMPTZ
    | INSTANT
    | TIMEO | DATETIMEO | TIMESTAMPO
    | UTCOFFSET | TIMEZONE
    | INTERVAL
    | TINYINT | SMALLINT | INT | NUMERIC | REAL | DOUBLE PRECISION? | CHAR | VARCHAR | NCHAR | TEXT
    ;

intervalSpecifier: ( fromItem=intervalSpecifierItem TO )? toItem=intervalSpecifierItem;
intervalSpecifierItem: intervalFieldName ( S_PAR_START integerLiteral S_PAR_END )?;
intervalFieldName: YEAR | MONTH | DAY | HOUR | MINUTE | SECOND;
scopeableFieldName: ( tableName S_DOT )? fieldName;
extendedValue: literal | variable;
variable: S_AT identifier;
fieldName: identifier;
tableName: identifier;
identifier: D_SIMPLENAME | D_QUOTEDNAME | D_BACKTICKEDNAME;
literal: NULL | stringLiteral | bitStringLiteral | integerLiteral | decimalLiteral | booleanLiteral;
integerLiteral: ( S_MINUS | S_PLUS )? D_INTEGER;
decimalLiteral: ( S_MINUS | S_PLUS )? D_DECIMAL;
bitStringLiteral: binaryStringTokenList | hexadecimalStringTokenList;
binaryStringTokenList: D_BSTRING binaryStringContinuation*;
binaryStringContinuation: D_BSTRING_CONTINUATION | D_BSTRING;
hexadecimalStringTokenList: D_XSTRING hexadecimalStringContinuation*;
hexadecimalStringContinuation: D_BSTRING_CONTINUATION | D_XSTRING_CONTINUATION | D_XSTRING;
booleanLiteral: TRUE | FALSE;
likePart: LIKE stringLiteral;
stringLiteral: stringTokenList | escapeStringTokenList;
stringTokenList: stringToken+;
stringToken: safeStringToken | D_STRING;
escapeStringTokenList: D_ESTRING escapeStringContinuation*;
escapeStringContinuation: safeStringToken | D_ESTRING;
safeStringToken: D_BSTRING_CONTINUATION | D_XSTRING_CONTINUATION | D_SIMPLE_STRING;
schemaName: identifier;
parentheses: S_PAR_START S_PAR_END;

SELECT: S E L E C T;
INSERT: I N S E R T;
REPLACE: R E P L A C E;
UPDATE: U P D A T E;
DELETE: D E L E T E;
SHOW: S H O W;
CALL: C A L L;
USE: U S E;
SET: S E T;

CAST: C A S T;
CONVERT: C O N V E R T;
TRIM: T R I M;
LEADING: L E A D I N G;
TRAILING: T R A I L I N G;
BOTH: B O T H;
SUBSTRING: S U B S T R I N G;
SUBSTR: S U B S T R;
FOR: F O R;
POSITION: P O S I T I O N;
IN: I N;
EXTRACT: E X T R A C T;

BOOL: B O O L;
BOOLEAN: B O O L E A N;
BIT: B I T;
INTEGER: I N T E G E R;
BIGINT: B I G I N T;
DEC: D E C;
DECIMAL: D E C I M A L;
FLOAT: F L O A T;
NVARCHAR: N V A R C H A R;
CLOB: C L O B;
BINARY: B I N A R Y;
VARBINARY: V A R B I N A R Y;
BYTEA: B Y T E A;
BLOB: B L O B;
DATE: D A T E;
TIME: T I M E;
DATETIME: D A T E T I M E;
TIMESTAMP: T I M E S T A M P;
TIMETZ: T I M E T Z;
DATETIMETZ: D A T E T I M E T Z;
TIMESTAMPTZ: T I M E S T A M P T Z;
INSTANT: I N S T A N T;
TIMEO: T I M E O;
DATETIMEO: D A T E T I M E O;
TIMESTAMPO: T I M E S T A M P O;
UTCOFFSET: U T C O F F S E T;
TIMEZONE: T I M E Z O N E;
INTERVAL: I N T E R V A L;
SECOND: S E C O N D;
MINUTE: M I N U T E;
HOUR: H O U R;
DAY: D A Y;
MONTH: M O N T H;
YEAR: Y E A R;
TIMEZONE_HOUR: T I M E Z O N E '_' H O U R;
TIMEZONE_MINUTE: T I M E Z O N E '_' M I N U T E;
WITH: W I T H;
WITHOUT: W I T H O U T;
ZONE: Z O N E;
TINYINT: T I N Y I N T;
SMALLINT: S M A L L I N T;
INT: I N T;
NUMERIC: N U M E R I C;
REAL: R E A L;
DOUBLE: D O U B L E;
PRECISION: P R E C I S I O N;
CHAR: C H A R;
VARCHAR: V A R C H A R;
NCHAR: N C H A R;
TEXT: T E X T;

SYSTEM_USER: S Y S T E M '_' U S E R;
SESSION_USER: S E S S I O N '_' U S E R;
CURRENT_USER: C U R R E N T '_' U S E R;
CURRENT_SCHEMA: C U R R E N T '_' S C H E M A;
CURRENT_CATALOG: C U R R E N T '_' C A T A L O G;
CURRENT_DATE: C U R R E N T '_' D A T E;
CURRENT_TIME: C U R R E N T '_' T I M E;
CURRENT_TIMESTAMP: C U R R E N T '_' T I M E S T A M P;
READONLY: R E A D O N L Y;
AUTOCOMMIT: A U T O C O M M I T;
IDENTITY: I D E N T I T Y;
LAST_INSERT_ID: L A S T '_' I N S E R T '_' I D;

AS: A S;
COUNT: C O U N T;
DEFAULT: D E F A U L T;
DISTINCT: D I S T I N C T;
FROM: F R O M;
UNIT: U N I T;
INTO: I N T O;
WHERE: W H E R E;
BETWEEN: B E T W E E N;
OVERLAPS: O V E R L A P S;
ORDER: O R D E R;
BY: B Y;
ASC: A S C;
DESC: D E S C;
NULLS: N U L L S;
FIRST: F I R S T;
LAST: L A S T;
OFFSET: O F F S E T;
LIMIT: L I M I T;
FETCH: F E T C H;
NEXT: N E X T;
ROW: R O W;
ROWS: R O W S;
ONLY: O N L Y;
VALUES: V A L U E S;
IS: I S;
NOT: N O T;
NULL: N U L L;
UNKNOWN: U N K N O W N;
SCHEMAS: S C H E M A S;
DATABASES: D A T A B A S E S;
TABLES: T A B L E S;
LEFT: L E F T;
RIGHT: R I G H T;
INNER: I N N E R;
OUTER: O U T E R;
JOIN: J O I N;
ON: O N;
UNION: U N I O N;

LIKE: L I K E;
ILIKE: I L I K E;
ESCAPE: E S C A P E;
RLIKE: R L I K E;
REGEXP: R E G E X P;

CASE: C A S E;
WHEN: W H E N;
THEN: T H E N;
ELSE: E L S E;
END: E N D;

AND: A N D;
OR: O R;
XOR: X O R;

MOD: M O D;
DIV: D I V;

TRUE: T R U E;
FALSE: F A L S E;

TO: T O;

D_SIMPLENAME: [\p{L}_] [\p{N}\p{L}_]* ;
D_QUOTEDNAME: '"' ( '""' | ~["] )* '"';
D_BACKTICKEDNAME: '`' ( '``' | ~[`] )* '`';

D_BSTRING: B F_BSTRING;
D_BSTRING_CONTINUATION: F_BSTRING;
fragment F_BSTRING: '\'' [01]* '\'';
D_XSTRING: X F_XSTRING;
D_XSTRING_CONTINUATION: F_XSTRING;
fragment F_XSTRING: '\'' [0-9A-Fa-f]* '\'';
D_SIMPLE_STRING: '\'' ~[']* '\'';
D_STRING: '\'' ( '\'\'' | ~['] )* '\'';
D_ESTRING: E '\'' ( '\\' . | '\'\'' | ~[\\'] )* '\'';
D_DECIMAL: ( [0-9]+ '.' [0-9]* | '.' [0-9]+ ) F_EXPONENT? | [0-9]+ F_EXPONENT;
fragment F_EXPONENT: E [-+]? [0-9]+;
D_INTEGER: [0-9]+;

S_DOT: '.';
S_COMMA: ',';
S_AT: '@';

S_DOUBLE_PIPE: '||';
S_DOUBLE_COLON: '::';

S_ET: '&';
S_PIPE: '|';
S_SHIFT_LEFT: '<<';
S_SHIFT_RIGHT: '>>';
S_TILDE: '~';
S_HASH: '#';

S_ASTERISK: '*';
S_PERCENT: '%';
S_SLASH: '/';

S_PLUS: '+';
S_MINUS: '-';

S_EQ: '=';
S_NEQ_ANG: '<>';
S_NEQ_BANG: '!=';

S_LESS: '<';
S_LEQ: '<=';
S_GREATER: '>';
S_GEQ: '>=';

S_PAR_START: '(';
S_PAR_END: ')';

H_WHITESPACE: [ \n\t\r]+ -> channel(HIDDEN);

fragment A: [Aa];
fragment B: [Bb];
fragment C: [Cc];
fragment D: [Dd];
fragment E: [Ee];
fragment F: [Ff];
fragment G: [Gg];
fragment H: [Hh];
fragment I: [Ii];
fragment J: [Jj];
fragment K: [Kk];
fragment L: [Ll];
fragment M: [Mm];
fragment N: [Nn];
fragment O: [Oo];
fragment P: [Pp];
fragment Q: [Qq];
fragment R: [Rr];
fragment S: [Ss];
fragment T: [Tt];
fragment U: [Uu];
fragment V: [Vv];
fragment W: [Ww];
fragment X: [Xx];
fragment Y: [Yy];
fragment Z: [Zz];
