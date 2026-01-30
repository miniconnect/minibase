grammar SqlQuery;

@header {
package hu.webarticum.minibase.query.query.antlr.grammar;
}

sqlQuery: (
    selectCountQuery |
    selectQuery |
    standaloneSelectQuery |
    showSpecialQuery |
    updateQuery |
    insertQuery |
    deleteQuery |
    showSchemasQuery |
    showTablesQuery |
    useQuery |
    setVariableQuery
) EOF ;

selectCountQuery: (
    SELECT COUNT PAR_START ( wildcardSelectItem | DISTINCT? scopeableFieldName ) PAR_END fieldAliasPart=aliasPart?
    FROM ( schemaName DOT )? tableName tableAliasPart=aliasPart?
    wherePart?
    limitPart?
);

selectQuery: (
    SELECT selectPart
    FROM ( schemaName DOT )? tableName tableAliasPart=aliasPart?
    joinPart*
    wherePart?
    orderByPart?
    offsetLimitPart?
);

joinPart: (
	( innerJoin | leftJoin )
	( targetSchemaName=schemaName DOT )? targetTableName=tableName tableAliasPart=aliasPart?
	ON scope1=tableName DOT field1=fieldName EQ scope2=tableName DOT field2=fieldName
);
innerJoin: INNER? JOIN;
leftJoin: LEFT OUTER? JOIN;

selectPart: selectItem ( COMMA selectItem )*;
selectItem: aliasableExpression | wildcardSelectItem;
wildcardSelectItem: ( tableName DOT )? ASTERISK;
offsetLimitPart: offsetPart limitPart?  | limitPart offsetPart? | commaLimitPart;
offsetPart: OFFSET limitParameter ( ROW | ROWS )?;
limitPart: ( LIMIT | FETCH ( FIRST | NEXT ) ) limitParameter ( ( ROW | ROWS ) ONLY? )?;
commaLimitPart: LIMIT offsetValue=limitParameter COMMA limitValue=limitParameter;
limitParameter: TOKEN_INTEGER | stringLiteral | variable;

standaloneSelectQuery: standaloneSelectRow ( UNION standaloneSelectRow )*;
standaloneSelectRow: SELECT aliasableExpression ( COMMA aliasableExpression )* ( FROM UNIT )?;

showSpecialQuery: ( SHOW | CALL ) specialSelectable aliasPart?;

updateQuery: UPDATE ( schemaName DOT )? tableName updatePart wherePart?;
updatePart: SET updateItem ( COMMA updateItem )*;
updateItem: fieldName EQ extendedValue;

insertQuery: ( INSERT | REPLACE ) INTO ( schemaName DOT )? tableName fieldList? VALUES insertValueList;
fieldList: PAR_START fieldName ( COMMA fieldName )* PAR_END;
insertValueList: PAR_START insertValue ( COMMA insertValue )* PAR_END;
insertValue: extendedValue | DEFAULT;

deleteQuery: DELETE FROM ( schemaName DOT )? tableName wherePart?;

showSchemasQuery: SHOW ( SCHEMAS | DATABASES ) likePart?;

showTablesQuery: SHOW TABLES ( FROM schemaName )? likePart?;

useQuery: USE schemaName;

setVariableQuery: SET variable EQ extendedValue;

wherePart: WHERE ( whereItem ( AND whereItem )* | PAR_START whereItem ( AND whereItem )* PAR_END );
whereItem: scopeableFieldName postfixCondition | PAR_START whereItem PAR_END;
postfixCondition: simpleRelation extendedValue | betweenRelation | isNull | isNotNull;
simpleRelation: EQ | LESS | LESS_EQ | GREATER| GREATER_EQ;
betweenRelation: BETWEEN firstValue=extendedValue AND secondValue=extendedValue;
isNull: IS ( NULL | UNKNOWN );
isNotNull: IS NOT ( NULL | UNKNOWN );
orderByPart: ORDER BY orderByItem ( COMMA orderByItem )*;
orderByItem: ( scopeableFieldName | orderByPosition ) ( ASC | DESC )? ( nullsFirst | nullsLast )?;
nullsFirst: NULLS FIRST;
nullsLast: NULLS LAST;
orderByPosition: TOKEN_INTEGER;
aliasableExpression: expression aliasPart?;
aliasPart: AS? alias=identifier;
expression:
    subExpression=expression DOUBLE_COLON typeConstruct |
    unaryArithmeticExpression |
    leftExpression=expression binaryOperator=( ASTERISK | MOD | PERCENT | DIV | SLASH ) rightExpression=expression |
    leftExpression=expression binaryOperator=( PLUS | MINUS ) rightExpression=expression |
    notOperator=NOT subExpression=expression |
    leftExpression=expression binaryOperator=AND rightExpression=expression |
    leftExpression=expression binaryOperator=XOR rightExpression=expression |
    leftExpression=expression binaryOperator=OR rightExpression=expression |
    leftExpression=expression binaryOperator=DOUBLE_PIPE rightExpression=expression |
    leftExpression=expression binaryOperator=( LESS | LESS_EQ | GREATER | GREATER_EQ ) rightExpression=expression |
    leftExpression=expression binaryOperator=( EQ | NEQ_ANG | NEQ_BANG ) rightExpression=expression |
    givenExpression=expression IN inValueList |
    PAR_START start1Expression=expression COMMA end1Expression=expression PAR_END
        OVERLAPS PAR_START start2Expression=expression COMMA end2Expression=expression PAR_END |
    givenExpression=expression BETWEEN minExpression=expression AND maxExpression=expression |
    subExpression=expression IS NOT? isNullOperator=( NULL | UNKNOWN ) |
    givenExpression=expression NOT? likeOperator=( LIKE | ILIKE ) patternExpression=expression ( ESCAPE escapeExpression=expression )? |
    givenExpression=expression NOT? regexpOperator=( REGEXP | RLIKE ) patternExpression=expression |
    caseExpression |
    COUNT PAR_START DISTINCT? ASTERISK PAR_END |
    COUNT PAR_START DISTINCT subExpression=expression PAR_END |
    intervalExpression |
    trimExpression |
    substringExpression |
    positionExpression |
    extractExpression |
    castExpression |
    atomicExpression;
unaryArithmeticExpression: ( PLUS | MINUS ) subExpression=expression;
inValueList: PAR_START expression ( COMMA expression )* PAR_END;
caseExpression: CASE (givenExpression=expression)? whenPart+ elsePart? END;
whenPart: WHEN conditionExpression=expression THEN resultExpression=expression;
elsePart: ELSE expression;
intervalExpression: INTERVAL ( integerLiteral | decimalLiteral | stringLiteral ) intervalSpecifier?;
trimExpression: TRIM PAR_START trimSpecification? charsExpression=expression? FROM inputExpression=expression PAR_END;
trimSpecification: LEADING | TRAILING | BOTH;
substringExpression: ( SUBSTRING | SUBSTR ) PAR_START inputExpression=expression
    ( FROM fromExpression=expression ( FOR forExpression=expression )? | FOR forExpression=expression ) PAR_END;
positionExpression: POSITION PAR_START subjectExpression=expression IN contextExpression=expression PAR_END;
extractExpression: EXTRACT PAR_START extractFieldName FROM inputExpression=expression PAR_END;
extractFieldName: YEAR | MONTH | DAY | HOUR | MINUTE | SECOND | TIMEZONE_HOUR | TIMEZONE_MINUTE;
castExpression:
    CAST PAR_START expression AS typeConstruct PAR_END |
    CONVERT PAR_START expression COMMA typeConstruct PAR_END |
    CONVERT PAR_START typeConstruct COMMA expression PAR_END;
typeConstruct: simpleTypeConstruct | intervalTypeConstruct;
simpleTypeConstruct: typeName ( PAR_START ( size=sizeParameter ( COMMA scale=sizeParameter )? )? PAR_END )?;
intervalTypeConstruct: INTERVAL intervalSpecifier;
sizeParameter: TOKEN_INTEGER | stringLiteral;
atomicExpression:
    literal |
    variable |
    specialSelectable |
    scopeableFieldName |
    functionCall |
    PAR_START paredExpression=expression PAR_END;
specialSelectable: specialSelectableName ( parentheses )?;
specialSelectableName:
    CURRENT_USER |
    CURRENT_SCHEMA |
    CURRENT_CATALOG |
    READONLY |
    AUTOCOMMIT |
    IDENTITY |
    LAST_INSERT_ID;
functionCall: functionName PAR_START ( expression ( COMMA expression )* )? PAR_END;
functionName: identifier | functionNameToken;
functionNameToken: LEFT | RIGHT | TRIM | SUBSTRING | SUBSTR | typeName;
typeName:
    NULL | BOOLEAN | INTEGER | BIGINT | DEC | DECIMAL | FLOAT | NVARCHAR | CLOB | BINARY | VARBINARY | BLOB | DATE |
    ( TIME | DATETIME | TIMESTAMP ) ( ( WITH | WITHOUT ) ( TIME ZONE | OFFSET | UTCOFFSET ) )? |
    TIMETZ | DATETIMETZ | TIMESTAMPTZ |
    INSTANT |
    OTIME | ODATETIME | OTIMESTAMP |
    UTCOFFSET | TIMEZONE |
    INTERVAL |
    TINYINT | SMALLINT | INT | NUMERIC | REAL | DOUBLE PRECISION? | CHAR | VARCHAR | NCHAR | TEXT;
intervalSpecifier: ( fromItem=intervalSpecifierItem TO )? toItem=intervalSpecifierItem;
intervalSpecifierItem: intervalFieldName ( PAR_START integerLiteral PAR_END )?;
intervalFieldName: YEAR | MONTH | DAY | HOUR | MINUTE | SECOND;
scopeableFieldName: ( tableName DOT )? fieldName;
extendedValue: literal | variable;
variable: AT identifier;
fieldName: identifier;
tableName: identifier;
identifier: TOKEN_SIMPLENAME | TOKEN_QUOTEDNAME | TOKEN_BACKTICKEDNAME;
literal: NULL | stringLiteral | integerLiteral | decimalLiteral | booleanLiteral;
integerLiteral: ( MINUS | PLUS )? TOKEN_INTEGER;
decimalLiteral: ( MINUS | PLUS )? TOKEN_DECIMAL;
booleanLiteral: TRUE | FALSE;
likePart: LIKE stringLiteral;
stringLiteral: stringTokenList | escapeStringTokenList;
stringTokenList: TOKEN_STRING+;
escapeStringTokenList: TOKEN_ESTRING escapeStringContinuation*;
escapeStringContinuation: TOKEN_STRING | TOKEN_ESTRING_CONTINUATION;
schemaName: identifier;
parentheses: PAR_START PAR_END;

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

BOOLEAN: B O O L E A N;
INTEGER: I N T E G E R;
BIGINT: B I G I N T;
DEC: D E C;
DECIMAL: D E C I M A L;
FLOAT: F L O A T;
NVARCHAR: N V A R C H A R;
CLOB: C L O B;
BINARY: B I N A R Y;
VARBINARY: V A R B I N A R Y;
BLOB: B L O B;
DATE: D A T E;
TIME: T I M E;
DATETIME: D A T E T I M E;
TIMESTAMP: T I M E S T A M P;
TIMETZ: T I M E T Z;
DATETIMETZ: D A T E T I M E T Z;
TIMESTAMPTZ: T I M E S T A M P T Z;
INSTANT: I N S T A N T;
OTIME: O T I M E;
ODATETIME: O D A T E T I M E;
OTIMESTAMP: O T I M E S T A M P;
UTCOFFSET: U T C O F F S E T;
TIMEZONE: T I M E Z O N E;
INTERVAL: I N T E R V A L;
SECOND: S E C O N D;
MINUTE: M I N U T E;
HOUR: H O U R;
DAY: D A Y;
MONTH: M O N T H;
YEAR: Y E A R;
TIMEZONE_HOUR: T I M E Z O N E UNDERSCORE H O U R;
TIMEZONE_MINUTE: T I M E Z O N E UNDERSCORE M I N U T E;
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

CURRENT_USER: C U R R E N T UNDERSCORE U S E R;
CURRENT_SCHEMA: C U R R E N T UNDERSCORE S C H E M A;
CURRENT_CATALOG: C U R R E N T UNDERSCORE C A T A L O G;
READONLY: R E A D O N L Y;
AUTOCOMMIT: A U T O C O M M I T;
IDENTITY: I D E N T I T Y;
LAST_INSERT_ID: L A S T UNDERSCORE I N S E R T UNDERSCORE I D;

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

TOKEN_SIMPLENAME: [\p{L}_] [\p{N}\p{L}_]* ;
TOKEN_QUOTEDNAME: '"' ( '""' | ~["] )* '"';
TOKEN_BACKTICKEDNAME: '`' ( '``' | ~[`] )* '`';

TOKEN_STRING: '\'' ( '\'\'' | ~['] )* '\'';
TOKEN_ESTRING: E FRAGMENT_ESTRING;
TOKEN_ESTRING_CONTINUATION: FRAGMENT_ESTRING;
fragment FRAGMENT_ESTRING: '\'' ( '\\' . | '\'\'' | ~[\\'] )* '\'';
TOKEN_DECIMAL: ( [0-9]+ '.' [0-9]* | '.' [0-9]+ ) FRAGMENT_EXPONENT? | [0-9]+ FRAGMENT_EXPONENT;
fragment FRAGMENT_EXPONENT: E [-+]? [0-9]+;
TOKEN_INTEGER: [0-9]+;

DOT: '.';
COMMA: ',';
AT: '@';

DOUBLE_PIPE: '||';
DOUBLE_COLON: '::';

ASTERISK: '*';
PERCENT: '%';
SLASH: '/';

PLUS: '+';
MINUS: '-';

EQ: '=';
NEQ_ANG: '<>';
NEQ_BANG: '!=';

LESS: '<';
LESS_EQ: '<=';
GREATER: '>';
GREATER_EQ: '>=';

PAR_START: '(';
PAR_END: ')';

WHITESPACE: [ \n\t\r]+ -> channel(HIDDEN);

fragment UNDERSCORE: [_];

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
