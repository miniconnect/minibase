grammar SqlQuery;

@header {
package hu.webarticum.minibase.query.query.antlr.grammar;
}

sqlQuery: (
    selectQuery |
    selectCountQuery |
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

selectQuery: (
    SELECT selectPart
    FROM ( schemaName DOT )? tableName ( AS? tableAlias=identifier )?
    joinPart*
    wherePart?
    orderByPart?
    limitPart?
);

joinPart: (
	( innerJoin | leftJoin )
	( targetSchemaName=schemaName DOT )? targetTableName=tableName ( AS? tableAlias=identifier )?
	ON scope1=tableName DOT field1=fieldName EQ scope2=tableName DOT field2=fieldName
);
innerJoin: INNER? JOIN;
leftJoin: LEFT OUTER? JOIN;

selectCountQuery: (
    SELECT COUNT PAR_START ( wildcardSelectItem | DISTINCT? scopeableFieldName ) PAR_END
    FROM ( schemaName DOT )? tableName ( AS? tableAlias=identifier )?
    wherePart?
);

selectPart: selectItem ( COMMA selectItem )*;
selectItem: aliasableExpression | wildcardSelectItem;
wildcardSelectItem: ( tableName DOT )? ASTERISK;
limitPart: ( LIMIT | FETCH FIRST ) ( TOKEN_INTEGER | TOKEN_STRING | variable ) ( ( ROW | ROWS ) ONLY )?;

standaloneSelectQuery: standaloneSelectRow ( UNION standaloneSelectRow )*;
standaloneSelectRow: SELECT aliasableExpression ( COMMA aliasableExpression )* ( FROM UNIT )?;

showSpecialQuery: ( SHOW | CALL ) specialSelectable ( AS? alias=identifier )?;

updateQuery: UPDATE ( schemaName DOT )? tableName updatePart wherePart?;
updatePart: SET updateItem ( COMMA updateItem )*;
updateItem: fieldName EQ extendedValue;

insertQuery: ( INSERT | REPLACE ) INTO ( schemaName DOT )? tableName fieldList? VALUES valueList;
fieldList: PAR_START fieldName ( COMMA fieldName )* PAR_END;
valueList: PAR_START extendedValue ( COMMA extendedValue )* PAR_END;

deleteQuery: DELETE FROM ( schemaName DOT )? tableName wherePart?;

showSchemasQuery: SHOW ( SCHEMAS | DATABASES ) likePart?;

showTablesQuery: SHOW TABLES ( FROM schemaName )? likePart?;

useQuery: USE schemaName;

setVariableQuery: SET variable EQ extendedValue;

wherePart: WHERE whereItem ( AND whereItem )*;
whereItem: scopeableFieldName postfixCondition | PAR_START whereItem PAR_END;
postfixCondition: simpleRelation extendedValue | betweenRelation | isNull | isNotNull;
simpleRelation: EQ | LESS | LESS_EQ | GREATER| GREATER_EQ;
betweenRelation: BETWEEN firstValue=extendedValue AND secondValue=extendedValue;
isNull: IS NULL;
isNotNull: IS NOT NULL;
orderByPart: ORDER BY orderByItem ( COMMA orderByItem )*;
orderByItem: ( scopeableFieldName | orderByPosition ) ( ASC | DESC )? ( nullsFirst | nullsLast )?;
nullsFirst: NULLS FIRST;
nullsLast: NULLS LAST;
orderByPosition: TOKEN_INTEGER;
aliasableExpression: expression ( AS? alias=identifier )?;
expression:
    leftExpression=expression ( ASTERISK | MOD | PERCENT | DIV | SLASH ) rightExpression=expression |
    leftExpression=expression ( PLUS | MINUS ) rightExpression=expression |
    atomicExpression;
atomicExpression:
    NULL |
    TOKEN_STRING |
    TOKEN_INTEGER |
    variable |
    specialSelectable |
    scopeableFieldName |
    functionCall |
    PAR_START paredExpression=expression PAR_END |
    MINUS negatedExpression=expression;
specialSelectable: specialSelectableName ( parentheses )?;
specialSelectableName:
    CURRENT_USER |
    CURRENT_SCHEMA |
    CURRENT_CATALOG |
    READONLY |
    AUTOCOMMIT |
    LAST_INSERT_ID;
functionCall: identifier PAR_START expression ( COMMA expression )* PAR_END;
scopeableFieldName: ( tableName DOT )? fieldName;
extendedValue: literal | variable | NULL;
variable: AT identifier;
fieldName: identifier;
tableName: identifier;
identifier: TOKEN_SIMPLENAME | TOKEN_QUOTEDNAME | TOKEN_BACKTICKEDNAME;
literal: TOKEN_STRING | TOKEN_INTEGER;
likePart: LIKE TOKEN_STRING;
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

CURRENT_USER: C U R R E N T UNDERSCORE U S E R;
CURRENT_SCHEMA: C U R R E N T UNDERSCORE S C H E M A;
CURRENT_CATALOG: C U R R E N T UNDERSCORE C A T A L O G;
READONLY: R E A D O N L Y;
AUTOCOMMIT: A U T O C O M M I T;
IDENTITY: I D E N T I T Y;
LAST_INSERT_ID: L A S T UNDERSCORE I N S E R T UNDERSCORE I D;

AS: A S;
COUNT: C O U N T;
DISTINCT: D I S T I N C T;
FROM: F R O M;
UNIT: U N I T;
INTO: I N T O;
WHERE: W H E R E;
AND: A N D;
BETWEEN: B E T W E E N;
ORDER: O R D E R;
BY: B Y;
ASC: A S C;
DESC: D E S C;
NULLS: N U L L S;
FIRST: F I R S T;
LAST: L A S T;
LIMIT: L I M I T;
FETCH: F E T C H;
ROW: R O W;
ROWS: R O W S;
ONLY: O N L Y;
VALUES: V A L U E S;
IS: I S;
NOT: N O T;
NULL: N U L L;
SCHEMAS: S C H E M A S;
DATABASES: D A T A B A S E S;
TABLES: T A B L E S;
LIKE: L I K E;
LEFT: L E F T;
INNER: I N N E R;
OUTER: O U T E R;
JOIN: J O I N;
ON: O N;
UNION: U N I O N;

MOD: M O D;
DIV: D I V;

TOKEN_SIMPLENAME: [\p{L}_] [\p{N}\p{L}_]* ;
TOKEN_QUOTEDNAME: '"' ( '\\' . | '""' | ~[\\"] )* '"';
TOKEN_BACKTICKEDNAME: '`' ( '``' | ~[`] )* '`';

TOKEN_STRING: '\'' ( '\\' . | '\'\'' | ~[\\'] )* '\'';
TOKEN_INTEGER: MINUS? [0-9]+;

DOT: '.';
COMMA: ',';
AT: '@';

ASTERISK: '*';
PERCENT: '%';
SLASH: '/';

PLUS: '+';
MINUS: '-';

EQ: '=';
LESS: '<';
LESS_EQ: '<=';
GREATER: '>';
GREATER_EQ: '>=';

PAR_START: '(';
PAR_END: ')';

WHITESPACE: [ \n\t\r] -> skip;

ANY: .;

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
