package hu.webarticum.minibase.query.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;

import hu.webarticum.minibase.query.expression.BinaryArithmeticExpression;
import hu.webarticum.minibase.query.expression.ColumnExpression;
import hu.webarticum.minibase.query.expression.ConcatExpression;
import hu.webarticum.minibase.query.expression.ConstantExpression;
import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.minibase.query.expression.NegateExpression;
import hu.webarticum.minibase.query.expression.SpecialValueExpression;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.expression.VariableExpression;
import hu.webarticum.minibase.query.query.DeleteQuery;
import hu.webarticum.minibase.query.query.InsertQuery;
import hu.webarticum.minibase.query.query.JoinType;
import hu.webarticum.minibase.query.query.NullCondition;
import hu.webarticum.minibase.query.query.NullsOrderMode;
import hu.webarticum.minibase.query.query.Queries;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.RangeCondition;
import hu.webarticum.minibase.query.query.SelectCountQuery;
import hu.webarticum.minibase.query.query.SelectQuery;
import hu.webarticum.minibase.query.query.SetVariableQuery;
import hu.webarticum.minibase.query.query.ShowSchemasQuery;
import hu.webarticum.minibase.query.query.ShowSpecialQuery;
import hu.webarticum.minibase.query.query.ShowTablesQuery;
import hu.webarticum.minibase.query.query.StandaloneSelectQuery;
import hu.webarticum.minibase.query.query.UpdateQuery;
import hu.webarticum.minibase.query.query.UseQuery;
import hu.webarticum.minibase.query.query.VariableValue;
import hu.webarticum.minibase.query.query.SelectQuery.JoinItem;
import hu.webarticum.minibase.query.query.SelectQuery.OrderByItem;
import hu.webarticum.minibase.query.query.SelectQuery.SelectItem;
import hu.webarticum.minibase.query.query.SelectQuery.ExpressionSelectItem;
import hu.webarticum.minibase.query.query.SelectQuery.WildcardSelectItem;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryLexer;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.AliasableExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.AtomicExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.BetweenRelationContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.DeleteQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ExtendedValueContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FieldListContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FieldNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FunctionCallContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.IdentifierContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.InsertQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.JoinPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LikePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LimitPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LiteralContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.OrderByItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.OrderByPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.OrderByPositionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.PostfixConditionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SchemaNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ScopeableFieldNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SelectCountQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SelectItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SelectPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SelectQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SetVariableQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ShowSchemasQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ShowSpecialQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ShowTablesQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SimpleRelationContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SpecialSelectableContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SqlQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.StandaloneSelectQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.StandaloneSelectRowContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.TableNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdateItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdatePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdateQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UseQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ValueListContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.VariableContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WhereItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WherePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WildcardSelectItemContext;

public class AntlrSqlParser implements SqlParser {

    private static final Pattern UNQUOTE_PATTERN = Pattern.compile("\\\\(.)");
    
    
    @Override
    public Query parse(String sql) {
        SqlQueryLexer lexer = new SqlQueryLexer(CharStreams.fromString(sql));
        SqlQueryParser parser = new SqlQueryParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new ParseErrorListener());
        SqlQueryContext rootNode = parser.sqlQuery();
        return parseRootNode(rootNode);
    }

    private Query parseRootNode(SqlQueryContext rootNode) {
        SelectQueryContext selectQueryNode = rootNode.selectQuery();
        if (selectQueryNode != null) {
            return parseSelectNode(selectQueryNode);
        }

        SelectCountQueryContext selectCountQueryNode = rootNode.selectCountQuery();
        if (selectCountQueryNode != null) {
            return parseSelectCountNode(selectCountQueryNode);
        }
        
        StandaloneSelectQueryContext standaloneSelectQueryNode = rootNode.standaloneSelectQuery();
        if (standaloneSelectQueryNode != null) {
            return parseStandaloneSelectNode(standaloneSelectQueryNode);
        }
        
        ShowSpecialQueryContext showSpecialQueryNode = rootNode.showSpecialQuery();
        if (showSpecialQueryNode != null) {
            return parseShowSpecialNode(showSpecialQueryNode);
        }

        InsertQueryContext insertQueryNode = rootNode.insertQuery();
        if (insertQueryNode != null) {
            return parseInsertNode(insertQueryNode);
        }
        
        UpdateQueryContext updateQueryNode = rootNode.updateQuery();
        if (updateQueryNode != null) {
            return parseUpdateNode(updateQueryNode);
        }
        
        DeleteQueryContext deleteQueryNode = rootNode.deleteQuery();
        if (deleteQueryNode != null) {
            return parseDeleteNode(deleteQueryNode);
        }
        
        ShowSchemasQueryContext showSchemasQueryNode = rootNode.showSchemasQuery();
        if (showSchemasQueryNode != null) {
            return parseShowSchemasNode(showSchemasQueryNode);
        }

        ShowTablesQueryContext showTablesQueryNode = rootNode.showTablesQuery();
        if (showTablesQueryNode != null) {
            return parseShowTablesNode(showTablesQueryNode);
        }

        UseQueryContext useQueryNode = rootNode.useQuery();
        if (useQueryNode != null) {
            return parseUseNode(useQueryNode);
        }
        
        SetVariableQueryContext setVariableNode = rootNode.setVariableQuery();
        if (setVariableNode != null) {
            return parseSetVariableNode(setVariableNode);
        }
        
        throw new IllegalArgumentException("Query type not supported");
    }

    private SelectQuery parseSelectNode(SelectQueryContext selectQueryNode) {
        SelectPartContext selectPartNode = selectQueryNode.selectPart();
        SchemaNameContext schemaNameNode = selectQueryNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()) :
                null;
        IdentifierContext identifierNode = selectQueryNode.tableName().identifier();
        String tableName = parseIdentifierNode(identifierNode);
        String tableAlias = tableName;
        IdentifierContext aliasIdentifierNode = selectQueryNode.tableAlias;
        if (aliasIdentifierNode != null) {
            tableAlias = parseIdentifierNode(aliasIdentifierNode);
        }
        ImmutableList<SelectItem> selectItems = parseSelectPartNode(selectPartNode);
        List<JoinPartContext> joinParts = selectQueryNode.joinPart();
        ImmutableList<JoinItem> joins = parseJoinPartNodes(joinParts, tableAlias);
        WherePartContext wherePartNode = selectQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);
        OrderByPartContext orderByNode = selectQueryNode.orderByPart();
        ImmutableList<OrderByItem> orderBy = parseOrderByPartNode(orderByNode);
        LimitPartContext limitPartNode = selectQueryNode.limitPart();
        LargeInteger limit = limitPartNode != null ?
                parseBigIntegerNode(limitPartNode.TOKEN_INTEGER()) :
                null;
        
        return Queries.select()
                .selectItems(selectItems)
                .inSchema(schemaName)
                .from(tableName)
                .tableAlias(tableAlias)
                .joins(joins)
                .where(where)
                .orderBy(orderBy)
                .limit(limit)
                .build();
    }
    
    private SelectCountQuery parseSelectCountNode(SelectCountQueryContext selectCountQueryNode) {
        SchemaNameContext schemaNameNode = selectCountQueryNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()) :
                null;
        IdentifierContext identifierNode = selectCountQueryNode.tableName().identifier();
        String tableName = parseIdentifierNode(identifierNode);
        String tableAlias = tableName;
        IdentifierContext aliasIdentifierNode = selectCountQueryNode.tableAlias;
        if (aliasIdentifierNode != null) {
            tableAlias = parseIdentifierNode(aliasIdentifierNode);
        }
        
        WildcardSelectItemContext wildcardSelectItemNode = selectCountQueryNode.wildcardSelectItem();
        TableNameContext tableNameNode = wildcardSelectItemNode.tableName();
        if (tableNameNode != null) {
            checkTableNameNode(tableNameNode, tableAlias);
        }
        
        WherePartContext wherePartNode = selectCountQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);
        
        return Queries.selectCount()
                .inSchema(schemaName)
                .from(tableName)
                .where(where)
                .build();
    }
    
    private StandaloneSelectQuery parseStandaloneSelectNode(StandaloneSelectQueryContext standaloneSelectQueryNode) {
        List<String> aliases = new ArrayList<>();
        StandaloneSelectRowContext firstRowNode = standaloneSelectQueryNode.standaloneSelectRow(0);
        for (AliasableExpressionContext aliasableExpressionNode : firstRowNode.aliasableExpression()) {
            String alias = parseNullableIdentifierNode(aliasableExpressionNode.alias);
            aliases.add(alias);
        }
        int columnCount = aliases.size();
        
        List<List<Expression>> expressionMatrix = new ArrayList<>();
        for (StandaloneSelectRowContext standaloneSelectRowNode : standaloneSelectQueryNode.standaloneSelectRow()) {
            List<Expression> expressionRow = new ArrayList<>();
            List<AliasableExpressionContext> aliasableExpressionNodes = standaloneSelectRowNode.aliasableExpression();
            if (aliasableExpressionNodes.size() != columnCount) {
                throw new IllegalArgumentException("Non-matching row lengths");
            }
            for (AliasableExpressionContext aliasableExpressionNode : aliasableExpressionNodes) {
                ExpressionContext expressionNode = aliasableExpressionNode.expression();
                Expression expression = parseExpressionNode(expressionNode);
                expressionRow.add(expression);
            }
            expressionMatrix.add(expressionRow);
        }
        
        return Queries.standaloneSelect()
                .aliases(aliases)
                .expressionMatrix(expressionMatrix)
                .build();
    }
    
    private ShowSpecialQuery parseShowSpecialNode(ShowSpecialQueryContext selectSpecialQueryNode) {
        IdentifierContext aliasNode = selectSpecialQueryNode.alias;
        String alias = parseNullableIdentifierNode(aliasNode);
        
        SpecialSelectableContext specialSelectableNode = selectSpecialQueryNode.specialSelectable();
        String specialSelectableName = specialSelectableNode.specialSelectableName().getText().toUpperCase();
        SpecialValueExpression expression =
                new SpecialValueExpression(SpecialValueParameter.valueOf(specialSelectableName));
        
        return Queries.showSpecial()
                .specialValueExpression(expression)
                .alias(alias)
                .build();
    }

    private InsertQuery parseInsertNode(InsertQueryContext insertQueryNode) {
        boolean replace = (insertQueryNode.REPLACE() != null);
        SchemaNameContext schemaNameNode = insertQueryNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()) :
                null;
        IdentifierContext identifierNode = insertQueryNode.tableName().identifier();
        String tableName = parseIdentifierNode(identifierNode);
        FieldListContext fieldListNode = insertQueryNode.fieldList();
        ImmutableList<String> fields = parseInsertFieldListNode(fieldListNode);
        ValueListContext valueListNode = insertQueryNode.valueList();
        ImmutableList<Object> values = parseInsertValueListNode(valueListNode);
        
        return Queries.insert()
                .replace(replace)
                .inSchema(schemaName)
                .into(tableName)
                .fields(fields)
                .values(values)
                .build();
    }

    private ImmutableList<String> parseInsertFieldListNode(FieldListContext fieldListNode) {
        if (fieldListNode == null) {
            return null;
        }
        
        List<String> resultBuilder = new ArrayList<>();
        for (FieldNameContext fieldNameNode : fieldListNode.fieldName()) {
            String fieldName = parseIdentifierNode(fieldNameNode.identifier());
            resultBuilder.add(fieldName);
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    private ImmutableList<Object> parseInsertValueListNode(ValueListContext valueListNode) {
        List<Object> resultBuilder = new ArrayList<>();
        for (ExtendedValueContext nullableValueNode : valueListNode.extendedValue()) {
            Object value = parseExtendedValueNode(nullableValueNode);
            resultBuilder.add(value);
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    private UpdateQuery parseUpdateNode(UpdateQueryContext updateQueryNode) {
        SchemaNameContext schemaNameNode = updateQueryNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()) :
                null;
        IdentifierContext identifierNode = updateQueryNode.tableName().identifier();
        String tableName = parseIdentifierNode(identifierNode);
        UpdatePartContext updatePartNode = updateQueryNode.updatePart();
        LinkedHashMap<String, Object> values = parseUpdatePartNode(updatePartNode);
        WherePartContext wherePartNode = updateQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);

        return Queries.update()
                .inSchema(schemaName)
                .table(tableName)
                .set(values)
                .where(where)
                .build();
    }

    private DeleteQuery parseDeleteNode(DeleteQueryContext deleteQueryNode) {
        SchemaNameContext schemaNameNode = deleteQueryNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()) :
                null;
        IdentifierContext identifierNode = deleteQueryNode.tableName().identifier();
        String tableName = parseIdentifierNode(identifierNode);
        WherePartContext wherePartNode = deleteQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);
        
        return Queries.delete()
                .inSchema(schemaName)
                .from(tableName)
                .where(where)
                .build();
    }
    
    private ShowSchemasQuery parseShowSchemasNode(ShowSchemasQueryContext showSchemasNode) {
        LikePartContext likePartContext = showSchemasNode.likePart();
        String like = parseLikePart(likePartContext);
        
        return Queries.showSchemas()
                .like(like)
                .build();
    }

    private ShowTablesQuery parseShowTablesNode(ShowTablesQueryContext showTablesNode) {
        SchemaNameContext schemaNameNode = showTablesNode.schemaName();
        String schemaName = schemaNameNode != null ?
                parseIdentifierNode(schemaNameNode.identifier()):
                null;
        LikePartContext likePartContext = showTablesNode.likePart();
        String like = parseLikePart(likePartContext);
        
        return Queries.showTables()
                .from(schemaName)
                .like(like)
                .build();
    }

    private UseQuery parseUseNode(UseQueryContext useNode) {
        IdentifierContext identifierNode = useNode.schemaName().identifier();
        String schemaName = parseIdentifierNode(identifierNode);
        
        return Queries.use()
                .schema(schemaName)
                .build();
    }
    
    private SetVariableQuery parseSetVariableNode(SetVariableQueryContext setVariableNode) {
        IdentifierContext identifierNode = setVariableNode.variable().identifier();
        String variableName = parseIdentifierNode(identifierNode);
        ExtendedValueContext valueNode = setVariableNode.extendedValue();
        Object value = parseExtendedValueNode(valueNode);
        
        return Queries.setVariable()
                .name(variableName)
                .value(value)
                .build();
    }
    
    private String parseLikePart(LikePartContext likePartContext) {
        if (likePartContext == null) {
            return null;
        }
        return parseStringNode(likePartContext.TOKEN_STRING());
    }

    private ImmutableList<SelectItem> parseSelectPartNode(SelectPartContext selectPartNode) {
        List<SelectItemContext> selectItemNodes = selectPartNode.selectItem();
        List<SelectItem> resultBuilder = new ArrayList<>(selectItemNodes.size());
        for (SelectItemContext selectItemNode : selectItemNodes) {
            resultBuilder.add(parseSelectItemNode(selectItemNode));
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    private SelectItem parseSelectItemNode(SelectItemContext selectItemNode) {
        WildcardSelectItemContext wildcardSelectItemNode = selectItemNode.wildcardSelectItem();
        if (wildcardSelectItemNode != null) {
            return parseWildcardSelectItemNode(wildcardSelectItemNode);
        } else {
            return parseAliasableExpressionNode(selectItemNode.aliasableExpression());
        }
    }
    
    private WildcardSelectItem parseWildcardSelectItemNode(WildcardSelectItemContext wildcardSelectItemNode) {
        TableNameContext tableNameNode = wildcardSelectItemNode.tableName();
        String tableName = tableNameNode != null ? parseIdentifierNode(tableNameNode.identifier()) : null;
        
        // TODO: check table name?
        
        return new WildcardSelectItem(tableName);
    }

    private ExpressionSelectItem parseAliasableExpressionNode(AliasableExpressionContext aliasableExpressionNode) {
        ExpressionContext expressionNode = aliasableExpressionNode.expression();
        Expression expression = parseExpressionNode(expressionNode);
        String alias =
                aliasableExpressionNode.alias != null ? parseIdentifierNode(aliasableExpressionNode.alias) : null;
        return new ExpressionSelectItem(expression, alias);
    }

    private Expression parseExpressionNode(ExpressionContext expressionNode) {
        AtomicExpressionContext atomicExpressionNode = expressionNode.atomicExpression();
        if (atomicExpressionNode != null) {
            return parseAtomicExpressionNode(atomicExpressionNode);
        }

        ExpressionContext leftExpressionNode = expressionNode.leftExpression;
        if (leftExpressionNode == null) {
            throw new IllegalArgumentException("Left expression is null in: " + expressionNode.getText());
        }
        
        ExpressionContext rightExpressionNode = expressionNode.rightExpression;
        if (rightExpressionNode == null) {
            throw new IllegalArgumentException("Right expression is null in: " + expressionNode.getText());
        }

        BinaryArithmeticExpression.Operation operation = extractOperation(expressionNode);
        if (operation == null) {
            throw new IllegalArgumentException("Can not detect operation in: " + expressionNode.getText());
        }

        Expression leftExpression = parseExpressionNode(leftExpressionNode);
        Expression rightExpression = parseExpressionNode(rightExpressionNode);
        return new BinaryArithmeticExpression(operation, leftExpression, rightExpression);
    }
    
    private BinaryArithmeticExpression.Operation extractOperation(ExpressionContext expressionNode) {
        if (expressionNode.ASTERISK() != null) {
            return BinaryArithmeticExpression.Operation.MUL;
        } else if (expressionNode.MOD() != null) {
            return BinaryArithmeticExpression.Operation.MOD;
        } else if (expressionNode.PERCENT() != null) {
            return BinaryArithmeticExpression.Operation.MOD;
        } else if (expressionNode.DIV() != null) {
            return BinaryArithmeticExpression.Operation.DIV;
        } else if (expressionNode.SLASH() != null) {
            return BinaryArithmeticExpression.Operation.RAT;
        } else if (expressionNode.PLUS() != null) {
            return BinaryArithmeticExpression.Operation.ADD;
        } else if (expressionNode.MINUS() != null) {
            return BinaryArithmeticExpression.Operation.SUB;
        } else {
            return null;
        }
    }
    
    private Expression parseAtomicExpressionNode(AtomicExpressionContext atomicExpressionNode) {
        if (atomicExpressionNode.paredExpression != null) {
            return parseExpressionNode(atomicExpressionNode.paredExpression);
        }
        
        TerminalNode nullNode = atomicExpressionNode.NULL();
        if (nullNode != null) {
            return new ConstantExpression(null);
        }
        
        TerminalNode integerTokenNode = atomicExpressionNode.TOKEN_INTEGER();
        if (integerTokenNode != null) {
            Integer integerValue = parseIntegerNode(integerTokenNode);
            return new ConstantExpression(integerValue);
        }
        
        TerminalNode stringTokenNode = atomicExpressionNode.TOKEN_STRING();
        if (stringTokenNode != null) {
            String stringValue = parseStringNode(stringTokenNode);
            return new ConstantExpression(stringValue);
        }
        
        VariableContext variableNode = atomicExpressionNode.variable();
        if (variableNode != null) {
            String variableName = parseIdentifierNode(variableNode.identifier());
            return new VariableExpression(variableName);
        }
        
        SpecialSelectableContext specialSelectableNode = atomicExpressionNode.specialSelectable();
        if (specialSelectableNode != null) {
            String specialSelectableName = specialSelectableNode.specialSelectableName().getText().toUpperCase();
            return new SpecialValueExpression(SpecialValueParameter.valueOf(specialSelectableName));
        }
        
        ScopeableFieldNameContext scopeableFieldNameNode = atomicExpressionNode.scopeableFieldName();
        if (scopeableFieldNameNode != null) {
            TableNameContext tableNameNode = scopeableFieldNameNode.tableName();
            String tableAlias = tableNameNode != null ? parseIdentifierNode(tableNameNode.identifier()) : null;
            String columnName = parseIdentifierNode(scopeableFieldNameNode.fieldName().identifier());
            return new ColumnExpression(tableAlias, columnName);
        }
        
        FunctionCallContext functionCallNode = atomicExpressionNode.functionCall();
        if (functionCallNode != null) {
            String functionName = parseIdentifierNode(functionCallNode.identifier());
            ImmutableList<Expression> parameters = functionCallNode.expression().stream()
                    .map(this::parseExpressionNode)
                    .collect(ImmutableList.createCollector());
            // XXX
            if (functionName.equalsIgnoreCase("CONCAT")) {
                return new ConcatExpression(parameters);
            } else {
                throw new IllegalArgumentException("Unknown function: " + functionName);
            }
        }
        
        if (atomicExpressionNode.MINUS() != null) {
            Expression subExpression = parseExpressionNode(atomicExpressionNode.negatedExpression);
            return new NegateExpression(subExpression);
        }
        
        throw new IllegalArgumentException("Unknown expression: " + atomicExpressionNode.getText());
    }
    
    private ImmutableList<JoinItem> parseJoinPartNodes(
            List<JoinPartContext> joinPartNodes, String tableAlias) {
        Set<String> previousTableAliases = new HashSet<>(joinPartNodes.size() + 1);
        previousTableAliases.add(tableAlias);
        List<JoinItem> resultBuilder = new ArrayList<>(joinPartNodes.size()); 
        for (JoinPartContext joinPartNode : joinPartNodes) {
            resultBuilder.add(parseJoinPartNode(joinPartNode, previousTableAliases));
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    private JoinItem parseJoinPartNode(JoinPartContext joinPartNode, Set<String> previousTableAliases) {
        SchemaNameContext targetSchemaNameNode = joinPartNode.targetSchemaName;
        String targetSchemaName =
                targetSchemaNameNode != null ?
                parseIdentifierNode(targetSchemaNameNode.identifier()) :
                null;
        String targetTableName = parseIdentifierNode(joinPartNode.targetTableName.identifier());
        IdentifierContext tableAliasNode = joinPartNode.tableAlias;
        String targetTableAlias = tableAliasNode != null ? parseIdentifierNode(tableAliasNode) : targetTableName;
        String scope1 = parseIdentifierNode(joinPartNode.scope1.identifier());
        String field1 = parseIdentifierNode(joinPartNode.field1.identifier());
        String scope2 = parseIdentifierNode(joinPartNode.scope2.identifier());
        String field2 = parseIdentifierNode(joinPartNode.field2.identifier());
        
        if (scope1.equals(scope2)) {
            throw new IllegalArgumentException("Can not join to the same table alias: " + scope1);
        }
        
        String targetFieldName;
        String sourceTableAlias;
        String sourceFieldName;
        if (scope1.equals(targetTableAlias)) {
            targetFieldName = field1;
            sourceTableAlias = scope2;
            sourceFieldName = field2;
        } else if (scope2.equals(targetTableAlias)) {
            targetFieldName = field2;
            sourceTableAlias = scope1;
            sourceFieldName = field1;
        } else {
            throw new IllegalArgumentException(
                    "Try to use join table alias " + targetTableAlias +
                    ", but " + scope1 + " and " + scope2 + " found");
        }

        if (!previousTableAliases.contains(sourceTableAlias)) {
            throw new IllegalArgumentException("Unknown table alias: " + sourceTableAlias);
        }
        
        if (!previousTableAliases.add(targetTableAlias)) {
            throw new IllegalArgumentException("Duplicated table alias: " + targetTableAlias);
        }
        
        JoinType joinType = joinPartNode.leftJoin() != null ? JoinType.LEFT_OUTER : JoinType.INNER;
        
        return new JoinItem(
                joinType,
                targetSchemaName,
                targetTableName,
                targetTableAlias,
                targetFieldName,
                sourceTableAlias,
                sourceFieldName);
    }
    
    private ImmutableList<WhereItem> parseWherePartNode(WherePartContext wherePartNode) {
        if (wherePartNode == null) {
            return ImmutableList.empty();
        }
        
        List<WhereItemContext> whereItemNodes = wherePartNode.whereItem();
        List<WhereItem> resultBuilder = new ArrayList<>(whereItemNodes.size());
        for (WhereItemContext whereItemNode : whereItemNodes) {
            resultBuilder.add(parseWhereItemNode(whereItemNode));
        }
        return ImmutableList.fromCollection(resultBuilder);
    }
    
    private WhereItem parseWhereItemNode(WhereItemContext whereItemNode) {
        WhereItemContext subItemNode = whereItemNode.whereItem();
        if (subItemNode != null) {
            return parseWhereItemNode(subItemNode);
        }

        ScopeableFieldNameContext scopeableFieldNameNode = whereItemNode.scopeableFieldName();
        String fieldName = parseIdentifierNode(scopeableFieldNameNode.fieldName().identifier());
        TableNameContext tableNameNode = scopeableFieldNameNode.tableName();
        String tableName = tableNameNode != null ? parseIdentifierNode(tableNameNode.identifier()) : null;
        
        // TODO: check table name?
        
        Object value = parsePostfixConditionNode(whereItemNode.postfixCondition());
        return new WhereItem(tableName, fieldName, value);
    }
    
    private ImmutableList<OrderByItem> parseOrderByPartNode(OrderByPartContext orderByPartNode) {
        if (orderByPartNode == null) {
            return ImmutableList.empty();
        }

        List<OrderByItemContext> orderByItemNodes = orderByPartNode.orderByItem();
        List<OrderByItem> resultBuilder = new ArrayList<>(orderByItemNodes.size());
        for (OrderByItemContext orderByItemNode : orderByItemNodes) {
            OrderByItem orderByItem = parseOrderByItemNode(orderByItemNode);
            resultBuilder.add(orderByItem);
        }
        return ImmutableList.fromCollection(resultBuilder);
    }
    
    private OrderByItem parseOrderByItemNode(OrderByItemContext orderByItemNode) {
        boolean ascOrder = (orderByItemNode.DESC() == null);
        
        NullsOrderMode nullsOrderMode;
        if (orderByItemNode.nullsFirst() != null) {
            nullsOrderMode = NullsOrderMode.NULLS_FIRST;
        } else if (orderByItemNode.nullsLast() != null) {
            nullsOrderMode = NullsOrderMode.NULLS_LAST;
        } else {
            nullsOrderMode = NullsOrderMode.NULLS_AUTO;
        }
        
        OrderByPositionContext orderByPositionNode = orderByItemNode.orderByPosition();
        if (orderByPositionNode != null) {
            Integer orderByPosition = parseIntegerNode(orderByPositionNode.TOKEN_INTEGER());
            return new OrderByItem(null, null, orderByPosition, ascOrder, nullsOrderMode);
        }
        
        ScopeableFieldNameContext scopeableFieldNameNode = orderByItemNode.scopeableFieldName();
        String fieldName = parseIdentifierNode(scopeableFieldNameNode.fieldName().identifier());
        
        TableNameContext tableNameNode = scopeableFieldNameNode.tableName();
        String tableName = tableNameNode != null ? parseIdentifierNode(tableNameNode.identifier()) : null;
        
        return new OrderByItem(tableName, fieldName, null, ascOrder, nullsOrderMode);
    }
    
    private LinkedHashMap<String, Object> parseUpdatePartNode(UpdatePartContext updatePartNode) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (UpdateItemContext updateItemNode : updatePartNode.updateItem()) {
            String fieldName = parseIdentifierNode(updateItemNode.fieldName().identifier());
            Object value = parseExtendedValueNode(updateItemNode.extendedValue());
            result.put(fieldName, value);
        }
        return result;
    }
    
    private void checkTableNameNode(TableNameContext tableNameNode, String expectedTableName) {
        if (tableNameNode == null) {
            return;
        }
        
        String fieldTableName = parseIdentifierNode(tableNameNode.identifier());
        if (!fieldTableName.equals(expectedTableName)) {
            throw new IllegalArgumentException("Unknown table: " + fieldTableName);
        }
    }

    private String parseNullableIdentifierNode(IdentifierContext identifierNode) {
        if (identifierNode == null) {
            return null;
        }
        
        return parseIdentifierNode(identifierNode);
    }
    
    private String parseIdentifierNode(IdentifierContext identifierNode) {
        TerminalNode simpleNameNode = identifierNode.TOKEN_SIMPLENAME();
        if (simpleNameNode != null) {
            return simpleNameNode.getText();
        }
        
        TerminalNode quotedNameNode = identifierNode.TOKEN_QUOTEDNAME();
        if (quotedNameNode != null) {
            return unquote(quotedNameNode.getText());
        }
        
        TerminalNode backtickedNameNode = identifierNode.TOKEN_BACKTICKEDNAME();
        if (backtickedNameNode != null) {
            return unbacktick(backtickedNameNode.getText());
        }
        
        throw new IllegalArgumentException("Invalid identifier: " + identifierNode.getText());
    }

    private Object parsePostfixConditionNode(PostfixConditionContext postfixConditionNode) {
        SimpleRelationContext simpleRelationNode = postfixConditionNode.simpleRelation();
        if (simpleRelationNode != null) {
            ExtendedValueContext extendedValueNode = postfixConditionNode.extendedValue();
            Object value = parseExtendedValueNode(extendedValueNode);
            if (simpleRelationNode.EQ() != null) {
                return value;
            } else {
                return buildHalfRangeCondition(simpleRelationNode, value);
            }
        }
        
        BetweenRelationContext betweenRelationNode = postfixConditionNode.betweenRelation();
        if (betweenRelationNode != null) {
            return parseBetweenRelationNode(betweenRelationNode);
        }
        
        if (postfixConditionNode.isNull() != null) {
            return NullCondition.IS_NULL;
        } else if (postfixConditionNode.isNotNull() != null) {
            return NullCondition.IS_NOT_NULL;
        }
        
        throw new IllegalArgumentException("Invalid postfix condition: " + postfixConditionNode.getText());
    }
    
    private Object buildHalfRangeCondition(SimpleRelationContext simpleRelationNode, Object value) {
        if (simpleRelationNode.LESS() != null) {
            return new RangeCondition(null, false, value, false);
        } else if (simpleRelationNode.LESS_EQ() != null) {
            return new RangeCondition(null, false, value, true);
        } else if (simpleRelationNode.GREATER() != null) {
            return new RangeCondition(value, false, null, false);
        } else if (simpleRelationNode.GREATER_EQ() != null) {
            return new RangeCondition(value, true, null, false);
        } else {
            throw new IllegalArgumentException("Invalid range condition: " + simpleRelationNode.getText());
        }
    }

    private Object parseExtendedValueNode(ExtendedValueContext extendedValueNode) {
        if (extendedValueNode.NULL() != null) {
            return null;
        }
        
        LiteralContext literalNode = extendedValueNode.literal();
        if (literalNode != null) {
            return parseLiteralNode(literalNode);
        }
        
        VariableContext variableNode = extendedValueNode.variable();
        if (variableNode != null) {
            String variableName = parseIdentifierNode(variableNode.identifier());
            return new VariableValue(variableName);
        }

        throw new IllegalArgumentException("Invalid value: " + extendedValueNode.getText());
    }

    private Object parseBetweenRelationNode(BetweenRelationContext betweenRelationNode) {
        Object firstValue = parseExtendedValueNode(betweenRelationNode.firstValue);
        Object secondValue = parseExtendedValueNode(betweenRelationNode.secondValue);
        return new RangeCondition(firstValue, true, secondValue, true);
    }
    
    private Object parseLiteralNode(LiteralContext literalNode) {
        TerminalNode integerNode = literalNode.TOKEN_INTEGER();
        if (integerNode != null) {
            return parseIntegerNode(integerNode);
        }
        
        TerminalNode stringNode = literalNode.TOKEN_STRING();
        if (stringNode != null) {
            return parseStringNode(stringNode);
        }
        
        throw new IllegalArgumentException("Invalid literal: " + literalNode.getText());
    }
    
    private Integer parseIntegerNode(TerminalNode integerNode) {
        return Integer.parseInt(integerNode.getText());
    }

    private LargeInteger parseBigIntegerNode(TerminalNode integerNode) {
        return LargeInteger.of(integerNode.getText());
    }

    private String parseStringNode(TerminalNode stringNode) {
        return unquote(stringNode.getText());
    }

    private static String unquote(String token) {
        int length = token.length();
        String innerPart = token.substring(1, length - 1);
        Matcher matcher = UNQUOTE_PATTERN.matcher(innerPart);
        return matcher.replaceAll("$1");
    }

    public static String unbacktick(String token) {
        int length = token.length();
        return token.substring(1, length - 1).replace("``", "`");
    }

    
    private static class ParseErrorListener extends BaseErrorListener {
        
        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String message,
                RecognitionException e) {
            String fullMessage = String.format(
                    "SQL syntax error at line %d at %d: %s",
                    line,
                    charPositionInLine,
                    message);
            throw new IllegalArgumentException(fullMessage, e);
        }
        
    }

}
