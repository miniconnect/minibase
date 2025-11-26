package hu.webarticum.minibase.query.parser;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import hu.webarticum.minibase.query.expression.AddExpression;
import hu.webarticum.minibase.query.expression.AndExpression;
import hu.webarticum.minibase.query.expression.BetweenExpression;
import hu.webarticum.minibase.query.expression.BinaryArithmeticExpression;
import hu.webarticum.minibase.query.expression.CaseExpression;
import hu.webarticum.minibase.query.expression.CastExpression;
import hu.webarticum.minibase.query.expression.CoalesceExpression;
import hu.webarticum.minibase.query.expression.ColumnExpression;
import hu.webarticum.minibase.query.expression.ConcatExpression;
import hu.webarticum.minibase.query.expression.ConstantExpression;
import hu.webarticum.minibase.query.expression.EqualsExpression;
import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.minibase.query.expression.IsNotNullExpression;
import hu.webarticum.minibase.query.expression.IsNullExpression;
import hu.webarticum.minibase.query.expression.LeftExpression;
import hu.webarticum.minibase.query.expression.LikeExpression;
import hu.webarticum.minibase.query.expression.NegateExpression;
import hu.webarticum.minibase.query.expression.NotEqualsExpression;
import hu.webarticum.minibase.query.expression.NotExpression;
import hu.webarticum.minibase.query.expression.NullifExpression;
import hu.webarticum.minibase.query.expression.OrExpression;
import hu.webarticum.minibase.query.expression.OrderRelationExpression;
import hu.webarticum.minibase.query.expression.RegexpExpression;
import hu.webarticum.minibase.query.expression.RightExpression;
import hu.webarticum.minibase.query.expression.SpecialValueExpression;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.expression.SubtractExpression;
import hu.webarticum.minibase.query.expression.TypeConstruct;
import hu.webarticum.minibase.query.expression.TypeConstruct.SymbolAlias;
import hu.webarticum.minibase.query.expression.VariableExpression;
import hu.webarticum.minibase.query.expression.XorExpression;
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
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryLexer;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.AliasPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.AliasableExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.AtomicExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.BetweenRelationContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.BooleanLiteralContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.CaseExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.CastExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.CommaLimitPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.DecimalLiteralContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.DeleteQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ElsePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ExtendedValueContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FieldListContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FieldNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FunctionCallContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.FunctionNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.IdentifierContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.InsertQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.InsertValueContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.IntegerLiteralContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.IntervalExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.IntervalFieldNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.JoinPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LikePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LimitParameterContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LimitPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.LiteralContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.OffsetLimitPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.OffsetPartContext;
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
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SizeParameterContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SpecialSelectableContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.SqlQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.StandaloneSelectQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.StandaloneSelectRowContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.TableNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.TypeConstructContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.TypeNameContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UnaryArithmeticExpressionContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdateItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdatePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UpdateQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.UseQueryContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.ValueListContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.VariableContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WhenPartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WhereItemContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WherePartContext;
import hu.webarticum.minibase.query.query.antlr.grammar.SqlQueryParser.WildcardSelectItemContext;
import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;

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
        
        String tableAlias = parseAliasPartNode(selectQueryNode.tableAliasPart);
        if (tableAlias == null) {
            tableAlias = tableName;
        }
        
        ImmutableList<SelectItem> selectItems = parseSelectPartNode(selectPartNode);
        List<JoinPartContext> joinParts = selectQueryNode.joinPart();
        ImmutableList<JoinItem> joins = parseJoinPartNodes(joinParts, tableAlias);
        WherePartContext wherePartNode = selectQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);
        OrderByPartContext orderByPartNode = selectQueryNode.orderByPart();
        ImmutableList<OrderByItem> orderBy = parseOrderByPartNode(orderByPartNode);
        OffsetLimitPartContext offsetLimitNode = selectQueryNode.offsetLimitPart();
        Object[] offsetAndLimit = parseOffsetLimitPartNode(offsetLimitNode);
        Object offset = offsetAndLimit[0];
        Object limit = offsetAndLimit[1];
        
        return Queries.select()
                .selectItems(selectItems)
                .inSchema(schemaName)
                .from(tableName)
                .tableAlias(tableAlias)
                .joins(joins)
                .where(where)
                .orderBy(orderBy)
                .offset(offset)
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

        String tableAlias = parseAliasPartNode(selectCountQueryNode.tableAliasPart);
        if (tableAlias == null) {
            tableAlias = tableName;
        }
        
        TableNameContext tableNameNode = extractTableNameNode(selectCountQueryNode);
        if (tableNameNode != null) {
            checkTableNameNode(tableNameNode, tableAlias);
        }
        
        String fieldName = extractFieldName(selectCountQueryNode);
        String alias = extractAlias(selectCountQueryNode);
        
        WherePartContext wherePartNode = selectCountQueryNode.wherePart();
        ImmutableList<WhereItem> where = parseWherePartNode(wherePartNode);
        
        LimitPartContext limitPartNode = selectCountQueryNode.limitPart();
        Object limit = parseLimitPartNode(limitPartNode);
        
        return Queries.selectCount()
                .inSchema(schemaName)
                .from(tableName)
                .onField(fieldName)
                .as(alias)
                .where(where)
                .limit(limit)
                .build();
    }
    
    private TableNameContext extractTableNameNode(SelectCountQueryContext selectCountQueryNode) {
        WildcardSelectItemContext wildcardSelectItemNode = selectCountQueryNode.wildcardSelectItem();
        if (wildcardSelectItemNode != null) {
            return wildcardSelectItemNode.tableName();
        }
        
        ScopeableFieldNameContext scopeableFieldNameNode = selectCountQueryNode.scopeableFieldName();
        if (scopeableFieldNameNode != null) {
            return scopeableFieldNameNode.tableName();
        }
        
        return null;
    }

    private String extractFieldName(SelectCountQueryContext selectCountQueryNode) {
        ScopeableFieldNameContext scopeableFieldNameNode = selectCountQueryNode.scopeableFieldName();
        if (scopeableFieldNameNode != null) {
            return parseIdentifierNode(scopeableFieldNameNode.fieldName().identifier());
        }
        
        return null;
    }

    private String extractAlias(SelectCountQueryContext selectCountQueryNode) {
        return parseAliasPartNode(selectCountQueryNode.fieldAliasPart);
    }
    
    private StandaloneSelectQuery parseStandaloneSelectNode(StandaloneSelectQueryContext standaloneSelectQueryNode) {
        List<String> aliases = new ArrayList<>();
        StandaloneSelectRowContext firstRowNode = standaloneSelectQueryNode.standaloneSelectRow(0);
        for (AliasableExpressionContext aliasableExpressionNode : firstRowNode.aliasableExpression()) {
            String alias = parseAliasPartNode(aliasableExpressionNode.aliasPart());
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
        String alias = parseAliasPartNode(selectSpecialQueryNode.aliasPart());
        
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
        for (InsertValueContext insertValueNode : valueListNode.insertValue()) {
            ExtendedValueContext extendedValueNode = insertValueNode.extendedValue();
            if (extendedValueNode != null) {
                Object value = parseExtendedValueNode(extendedValueNode);
                resultBuilder.add(value);
            } else {
                resultBuilder.add(null);
            }
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
        return new WildcardSelectItem(tableName);
    }

    private ExpressionSelectItem parseAliasableExpressionNode(AliasableExpressionContext aliasableExpressionNode) {
        ExpressionContext expressionNode = aliasableExpressionNode.expression();
        Expression expression = parseExpressionNode(expressionNode);
        String alias = parseAliasPartNode(aliasableExpressionNode.aliasPart());
        return new ExpressionSelectItem(expression, alias);
    }

    private String parseAliasPartNode(AliasPartContext aliasPartNode) {
        if (aliasPartNode == null) {
            return null;
        }
        
        return parseIdentifierNode(aliasPartNode.alias);
    }
    
    private Expression parseExpressionNode(ExpressionContext expressionNode) {
        AtomicExpressionContext atomicExpressionNode = expressionNode.atomicExpression();
        if (atomicExpressionNode != null) {
            return parseAtomicExpressionNode(atomicExpressionNode);
        }

        UnaryArithmeticExpressionContext unaryArithmeticExpressionNode = expressionNode.unaryArithmeticExpression();
        if (unaryArithmeticExpressionNode != null) {
            return parseUnaryArithmeticExpressionNode(unaryArithmeticExpressionNode);
        }

        IntervalExpressionContext intervalExpressionNode = expressionNode.intervalExpression();
        if (intervalExpressionNode != null) {
            return parseIntervalExpressionNode(intervalExpressionNode);
        }

        CastExpressionContext castExpressionNode = expressionNode.castExpression();
        if (castExpressionNode != null) {
            return parseCastExpressionNode(castExpressionNode);
        }

        if (expressionNode.notOperator != null) {
            return new NotExpression(parseExpressionNode(expressionNode.subExpression));
        }

        if (expressionNode.isNullOperator != null) {
            Expression subExpression = parseExpressionNode(expressionNode.subExpression);
            if (expressionNode.NOT() == null) {
                return new IsNullExpression(subExpression);
            } else {
                return new IsNotNullExpression(subExpression);
            }
        }

        CaseExpressionContext caseExpressionNode = expressionNode.caseExpression();
        if (caseExpressionNode != null) {
            return parseCaseExpressionNode(caseExpressionNode);
        }

        if (expressionNode.likeOperator != null) {
            Expression givenExpression = parseExpressionNode(expressionNode.givenExpression);
            Expression patternExpression = parseExpressionNode(expressionNode.patternExpression);
            Expression escapeExpression =
                    expressionNode.escapeExpression != null ?
                    parseExpressionNode(expressionNode.escapeExpression) :
                    null;
            boolean caseInsensitive = expressionNode.ILIKE() != null;
            Expression likeExpression = new LikeExpression(givenExpression, patternExpression, escapeExpression, caseInsensitive);
            if (expressionNode.NOT() == null) {
                return likeExpression;
            } else {
                return new NotExpression(likeExpression);
            }
        }
        
        if (expressionNode.regexpOperator != null) {
            Expression givenExpression = parseExpressionNode(expressionNode.givenExpression);
            Expression patternExpression = parseExpressionNode(expressionNode.patternExpression);
            Expression regexpExpression = new RegexpExpression(givenExpression, patternExpression);
            if (expressionNode.NOT() == null) {
                return regexpExpression;
            } else {
                return new NotExpression(regexpExpression);
            }
        }

        if (expressionNode.BETWEEN() != null) {
            Expression givenExpression = parseExpressionNode(expressionNode.givenExpression);
            Expression minExpression = parseExpressionNode(expressionNode.minExpression);
            Expression maxExpression = parseExpressionNode(expressionNode.maxExpression);
            return new BetweenExpression(givenExpression, minExpression, maxExpression);
        }

        if (expressionNode.DOUBLE_COLON() != null) {
            Expression subExpression = parseExpressionNode(expressionNode.subExpression);
            TypeConstruct typeConstruct = parseTypeConstructNode(expressionNode.typeConstruct());
            return new CastExpression(subExpression, typeConstruct);
        }

        if (expressionNode.COUNT() != null) {
            throw new UnsupportedOperationException("Aggregation currently not supported (COUNT)");
        }

        Object operation = extractOperation(expressionNode);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown operation type in: " + expressionNode.getText());
        }

        Expression leftExpression = parseExpressionNode(expressionNode.leftExpression);
        Expression rightExpression = parseExpressionNode(expressionNode.rightExpression);

        if (operation == EqualsExpression.class) {
            return new EqualsExpression(leftExpression, rightExpression);
        } else if (operation == NotEqualsExpression.class) {
            return new NotEqualsExpression(leftExpression, rightExpression);
        } else if (operation instanceof OrderRelationExpression.Operation) {
            OrderRelationExpression.Operation relationOperation = (OrderRelationExpression.Operation) operation;
            return new OrderRelationExpression(relationOperation, leftExpression, rightExpression);
        } else if (operation instanceof BinaryArithmeticExpression.Operation) {
            BinaryArithmeticExpression.Operation arithmeticOperation = (BinaryArithmeticExpression.Operation) operation;
            return new BinaryArithmeticExpression(arithmeticOperation, leftExpression, rightExpression);
        } else if (operation == AddExpression.class) {
            return new AddExpression(leftExpression, rightExpression);
        } else if (operation == SubtractExpression.class) {
            return new SubtractExpression(leftExpression, rightExpression);
        } else if (operation == AndExpression.class) {
            return new AndExpression(leftExpression, rightExpression);
        } else if (operation == XorExpression.class) {
            return new XorExpression(leftExpression, rightExpression);
        } else if (operation == OrExpression.class) {
            return new OrExpression(leftExpression, rightExpression);
        } else if (operation == ConcatExpression.class) {
            return new ConcatExpression(ImmutableList.of(leftExpression, rightExpression));
        } else {
            throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
    
    private Object extractOperation(ExpressionContext expressionNode) {
        if (expressionNode.EQ() != null) {
            return EqualsExpression.class;
        } else if (expressionNode.NEQ_ANG() != null || expressionNode.NEQ_BANG() != null) {
            return NotEqualsExpression.class;
        } else if (expressionNode.LESS() != null) {
            return OrderRelationExpression.Operation.LESS;
        } else if (expressionNode.LESS_EQ() != null) {
            return OrderRelationExpression.Operation.LESS_EQ;
        } else if (expressionNode.GREATER() != null) {
            return OrderRelationExpression.Operation.GREATER;
        } else if (expressionNode.GREATER_EQ() != null) {
            return OrderRelationExpression.Operation.GREATER_EQ;
        } else if (expressionNode.ASTERISK() != null) {
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
            return AddExpression.class;
        } else if (expressionNode.MINUS() != null) {
            return SubtractExpression.class;
        } else if (expressionNode.AND() != null) {
            return AndExpression.class;
        } else if (expressionNode.XOR() != null) {
            return XorExpression.class;
        } else if (expressionNode.OR() != null) {
            return OrExpression.class;
        } else if (expressionNode.DOUBLE_PIPE() != null) {
            return ConcatExpression.class;
        } else {
            return null;
        }
    }
    
    private Expression parseAtomicExpressionNode(AtomicExpressionContext atomicExpressionNode) {
        if (atomicExpressionNode.paredExpression != null) {
            return parseExpressionNode(atomicExpressionNode.paredExpression);
        }
        
        LiteralContext literalNode = atomicExpressionNode.literal();
        if (literalNode != null) {
            Object literalValue = parseLiteralNode(literalNode);
            return new ConstantExpression(literalValue);
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
            return parseFunctionCallNode(functionCallNode);
        }
        
        throw new IllegalArgumentException("Unknown expression: " + atomicExpressionNode.getText());
    }

    public Expression parseFunctionCallNode(FunctionCallContext functionCallNode) {
        String functionName = parseFunctionNameNode(functionCallNode.functionName());
        ImmutableList<Expression> parameters = functionCallNode.expression().stream()
                .map(this::parseExpressionNode)
                .collect(ImmutableList.createCollector());
        if (functionName.equalsIgnoreCase("COALESCE")) {
            return new CoalesceExpression(parameters);
        } else if (functionName.equalsIgnoreCase("CONCAT")) {
            return new ConcatExpression(parameters);
        } else if (functionName.equalsIgnoreCase("NULLIF")) {
            checkFunctionParameterCount("NULLIF", 2, parameters);
            return new NullifExpression(parameters.get(0), parameters.get(1));
        } else if (functionName.equalsIgnoreCase("LEFT")) {
            checkFunctionParameterCount("LEFT", 2, parameters);
            return new LeftExpression(parameters.get(0), parameters.get(1));
        } else if (functionName.equalsIgnoreCase("RIGHT")) {
            checkFunctionParameterCount("RIGHT", 2, parameters);
            return new RightExpression(parameters.get(0), parameters.get(1));
        } else {
            throw new IllegalArgumentException("Unknown function: " + functionName);
        }
    }

    private String parseFunctionNameNode(FunctionNameContext fuctionNameNode) {
        IdentifierContext identifierNode = fuctionNameNode.identifier();
        if (identifierNode != null) {
            return parseIdentifierNode(identifierNode);
        } else {
            return fuctionNameNode.getText();
        }
    }

    private void checkFunctionParameterCount(String name, int expectedCount, ImmutableList<Expression> actualParameters) {
        int actualCount = actualParameters.size();
        if (expectedCount != actualCount) {
            throw new IllegalArgumentException("Function " + name + "expects " + expectedCount + "parameters, " + actualCount + " given");
        }
    }

    private Expression parseUnaryArithmeticExpressionNode(UnaryArithmeticExpressionContext unaryArithmeticExpressionNode) {
        Expression subExpression = parseExpressionNode(unaryArithmeticExpressionNode.subExpression);
        if (unaryArithmeticExpressionNode.MINUS() != null) {
            return new NegateExpression(subExpression);
        } else {
            return subExpression;
        }
    }

    private Expression parseIntervalExpressionNode(IntervalExpressionContext intervalExpressionNode) {
        TerminalNode intervalStringNode = intervalExpressionNode.TOKEN_STRING();
        if (intervalStringNode != null) {
            String intervalString = parseStringNode(intervalStringNode);
            return new ConstantExpression(DateTimeDelta.parse(intervalString));
        }

        if (intervalExpressionNode.SECOND() != null) {
            BigDecimal seconds = parseDecimalLiteralNode(intervalExpressionNode.decimalLiteral());
            return new ConstantExpression(DateTimeDeltaUtil.deltaify(seconds));
        }

        LargeInteger amount = parseIntegerLiteralNode(intervalExpressionNode.integerLiteral());
        IntervalFieldNameContext intervalFieldNameNode = intervalExpressionNode.intervalFieldName();
        if (intervalFieldNameNode.NANOSECOND() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofSeconds(0, amount.longValueExact())).normalized());
        } else if (intervalFieldNameNode.MICROSECOND() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofSeconds(0, amount.multiply(1000).longValueExact())).normalized());
        } else if (intervalFieldNameNode.MILLISECOND() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofSeconds(0, amount.multiply(1_000_000).longValueExact())).normalized());
        } else if (intervalFieldNameNode.SECOND() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofSeconds(amount.longValueExact())).normalized());
        } else if (intervalFieldNameNode.MINUTE() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofMinutes(amount.longValueExact())).normalized());
        } else if (intervalFieldNameNode.HOUR() != null) {
            return new ConstantExpression(DateTimeDelta.of(Duration.ofHours(amount.longValueExact())).normalized());
        } else if (intervalFieldNameNode.DAY() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofDays(amount.intValueExact())));
        } else if (intervalFieldNameNode.WEEK() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofDays(amount.multiply(7).intValueExact())));
        } else if (intervalFieldNameNode.MONTH() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofMonths(amount.intValueExact())).normalized());
        } else if (intervalFieldNameNode.QUARTER() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofMonths(amount.multiply(3).intValueExact())).normalized());
        } else if (intervalFieldNameNode.YEAR() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofYears(amount.intValueExact())));
        } else if (intervalFieldNameNode.DECADE() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofYears(amount.multiply(10).intValueExact())));
        } else if (intervalFieldNameNode.CENTURY() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofYears(amount.multiply(100).intValueExact())));
        } else if (intervalFieldNameNode.MILLENNIUM() != null) {
            return new ConstantExpression(DateTimeDelta.of(Period.ofYears(amount.multiply(1000).intValueExact())));
        }
        
        throw new IllegalArgumentException("Unexpected expression: " + intervalExpressionNode.getText());
    }

    private Expression parseCastExpressionNode(CastExpressionContext castExpressionNode) {
        Expression subExpression = parseExpressionNode(castExpressionNode.expression());
        TypeConstruct typeConstruct = parseTypeConstructNode(castExpressionNode.typeConstruct());
        return new CastExpression(subExpression, typeConstruct);
    }
    
    private TypeConstruct parseTypeConstructNode(TypeConstructContext typeConstructNode) {
        TypeConstruct.SymbolAlias symbolAlias = parseTypeNameNode(typeConstructNode.typeName());
        Integer size = typeConstructNode.size != null ? parseSizeParameterNode(typeConstructNode.size) : null;
        Integer scale = typeConstructNode.scale != null ? parseSizeParameterNode(typeConstructNode.scale) : null;
        return new TypeConstruct(symbolAlias, size, scale);
    }

    private SymbolAlias parseTypeNameNode(TypeNameContext typeNameNode) {
        StringBuilder aliasNameBuilder = new StringBuilder();
        boolean first = true;
        for (ParseTree child : typeNameNode.children) {
            if (first) {
                first = false;
            } else  {
                aliasNameBuilder.append('_');
            }
            aliasNameBuilder.append(child.getText().toUpperCase());
        }
        return SymbolAlias.valueOf(aliasNameBuilder.toString());
        
    }

    private Integer parseSizeParameterNode(SizeParameterContext sizeParameterNode) {
        TerminalNode integerToken = sizeParameterNode.TOKEN_INTEGER();
        if (integerToken != null) {
            return parseIntegerNode(integerToken).intValueExact();
        }

        TerminalNode stringToken = sizeParameterNode.TOKEN_STRING();
        if (stringToken != null) {
            return Integer.parseInt(parseStringNode(stringToken));
        } else {
            return null;
        }
    }
    
    private Expression parseCaseExpressionNode(CaseExpressionContext caseExpressionNode) {
        Expression givenExpression = null;
        if (caseExpressionNode.givenExpression != null) {
            givenExpression = parseExpressionNode(caseExpressionNode.givenExpression);
        }
        Expression elseExpression = null;
        ElsePartContext elsePartNode = caseExpressionNode.elsePart();
        if (elsePartNode != null) {
            elseExpression = parseExpressionNode(elsePartNode.expression());
        }
        
        ImmutableList<CaseExpression.WhenItem> whenItems =
                ImmutableList.fromCollection(caseExpressionNode.whenPart()).map(this::parseCaseWhenItem);

        return new CaseExpression(givenExpression, whenItems, elseExpression);
    }

    private CaseExpression.WhenItem parseCaseWhenItem(WhenPartContext whenPartNode) {
        Expression conditionExpression = parseExpressionNode(whenPartNode.conditionExpression);
        Expression resultExpression = parseExpressionNode(whenPartNode.resultExpression);
        return new CaseExpression.WhenItem(conditionExpression, resultExpression);
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
        
        String targetTableAlias = parseAliasPartNode(joinPartNode.tableAliasPart);
        if (targetTableAlias == null) {
            targetTableAlias = targetTableName;
        }
        
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
            LargeInteger largeOrderByPosition = parseIntegerNode(orderByPositionNode.TOKEN_INTEGER());
            Integer orderByPosition = largeOrderByPosition != null ? largeOrderByPosition.intValueExact() : null;
            return new OrderByItem(null, null, orderByPosition, ascOrder, nullsOrderMode);
        }
        
        ScopeableFieldNameContext scopeableFieldNameNode = orderByItemNode.scopeableFieldName();
        String fieldName = parseIdentifierNode(scopeableFieldNameNode.fieldName().identifier());
        
        TableNameContext tableNameNode = scopeableFieldNameNode.tableName();
        String tableName = tableNameNode != null ? parseIdentifierNode(tableNameNode.identifier()) : null;
        
        return new OrderByItem(tableName, fieldName, null, ascOrder, nullsOrderMode);
    }
    
    private Object[] parseOffsetLimitPartNode(OffsetLimitPartContext offsetLimitPartNode) {
        Object[] result = new Object[] { null, null };
        if (offsetLimitPartNode == null) {
            return result;
        }
        
        OffsetPartContext offsetPartNode = offsetLimitPartNode.offsetPart();
        if (offsetPartNode != null) {
            result[0] = parseLimitParameterNode(offsetPartNode.limitParameter());
        }
        
        LimitPartContext limitPartNode = offsetLimitPartNode.limitPart();
        if (limitPartNode != null) {
            result[1] = parseLimitParameterNode(limitPartNode.limitParameter());
        }
        
        CommaLimitPartContext commaLimitPartNode = offsetLimitPartNode.commaLimitPart();
        if (commaLimitPartNode != null) {
            result[0] = parseLimitParameterNode(commaLimitPartNode.offsetValue);
            result[1] = parseLimitParameterNode(commaLimitPartNode.limitValue);
        }
        
        return result;
    }
    
    private Object parseLimitPartNode(LimitPartContext limitPartNode) {
        if (limitPartNode == null) {
            return null;
        }
        
        LimitParameterContext limitParameterNode = limitPartNode.limitParameter();
        return parseLimitParameterNode(limitParameterNode);
    }

    private Object parseLimitParameterNode(LimitParameterContext limitParameterNode) {
        TerminalNode integerToken = limitParameterNode.TOKEN_INTEGER();
        if (integerToken != null) {
            return parseIntegerNode(integerToken);
        }

        TerminalNode stringToken = limitParameterNode.TOKEN_STRING();
        if (stringToken != null) {
            return parseStringNode(stringToken);
        } else {
            VariableContext variableNode = limitParameterNode.variable();
            String variableName = parseIdentifierNode(variableNode.identifier());
            return new VariableValue(variableName);
        }
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
        if (literalNode.NULL() != null) {
            return null;
        }
        
        IntegerLiteralContext integerLiteralNode = literalNode.integerLiteral();
        if (integerLiteralNode != null) {
            return parseIntegerLiteralNode(integerLiteralNode);
        }

        DecimalLiteralContext decimalLiteralNode = literalNode.decimalLiteral();
        if (decimalLiteralNode != null) {
            return parseDecimalLiteralNode(decimalLiteralNode);
        }

        TerminalNode stringNode = literalNode.TOKEN_STRING();
        if (stringNode != null) {
            return parseStringNode(stringNode);
        }
        
        BooleanLiteralContext booleanLiteralNode = literalNode.booleanLiteral();
        if (booleanLiteralNode != null) {
            return parseBooleanLiteralNode(booleanLiteralNode);
        }
        
        throw new IllegalArgumentException("Invalid literal: " + literalNode.getText());
    }

    private LargeInteger parseIntegerLiteralNode(IntegerLiteralContext integerLiteralNode) {
        boolean negate = integerLiteralNode.MINUS() != null;
        LargeInteger largeIntegerValue = parseIntegerNode(integerLiteralNode.TOKEN_INTEGER());
        return negate ? largeIntegerValue.negate() : largeIntegerValue;
    }

    private BigDecimal parseDecimalLiteralNode(DecimalLiteralContext decimalLiteralNode) {
        boolean negate = decimalLiteralNode.MINUS() != null;
        BigDecimal bigDecimalValue = parseDecimalNode(decimalLiteralNode.TOKEN_DECIMAL());
        return negate ? bigDecimalValue.negate() : bigDecimalValue;
    }

    private LargeInteger parseIntegerNode(TerminalNode integerNode) {
        return LargeInteger.of(integerNode.getText());
    }

    private BigDecimal parseDecimalNode(TerminalNode integerNode) {
        return new BigDecimal(integerNode.getText());
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
    
    public static Boolean parseBooleanLiteralNode(BooleanLiteralContext booleanLiteralNode) {
        return booleanLiteralNode.TRUE() != null;
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
