package hu.webarticum.minibase.query.query;

import java.util.Objects;

import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.miniconnect.lang.ImmutableList;

public final class SelectQuery implements Query {
    
    private final ImmutableList<SelectItem> selectItems;

    private final String schemaName;

    private final String tableName;

    private final String tableAlias;
    
    private final ImmutableList<JoinItem> joins;
    
    private final ImmutableList<WhereItem> where;
    
    private final ImmutableList<OrderByItem> orderBy;

    private final Object limit;
    
    
    private SelectQuery(SelectQueryBuilder builder) {
        this.selectItems = Objects.requireNonNull(builder.selectItems);
        this.schemaName = builder.schemaName;
        this.tableName = Objects.requireNonNull(builder.tableName);
        this.tableAlias = builder.tableAlias;
        this.joins = Objects.requireNonNull(builder.joins);
        this.where = Objects.requireNonNull(builder.where);
        this.orderBy = Objects.requireNonNull(builder.orderBy);
        this.limit = builder.limit;
    }
    
    public static SelectQueryBuilder builder() {
        return new SelectQueryBuilder();
    }
    

    public ImmutableList<SelectItem> selectItems() {
        return selectItems;
    }

    public String schemaName() {
        return schemaName;
    }

    public String tableName() {
        return tableName;
    }

    public String tableAlias() {
        return tableAlias;
    }

    public ImmutableList<JoinItem> join() {
        return joins;
    }
    
    public ImmutableList<WhereItem> where() {
        return where;
    }
    
    public ImmutableList<OrderByItem> orderBy() {
        return orderBy;
    }

    public Object limit() {
        return limit;
    }
    
    
    public static final class SelectQueryBuilder {
        
        private ImmutableList<SelectItem> selectItems = ImmutableList.of(new WildcardSelectItem(null));

        private String schemaName = null;

        private String tableName = null;

        private String tableAlias = null;
        
        private ImmutableList<JoinItem> joins = ImmutableList.empty();
        
        private ImmutableList<WhereItem> where = ImmutableList.empty();
        
        private ImmutableList<OrderByItem> orderBy = ImmutableList.empty();

        private Object limit = null;
        
        
        private SelectQueryBuilder() {
            // use builder()
        }
        
        
        public SelectQueryBuilder selectItems(ImmutableList<SelectItem> selectItems) {
            this.selectItems = selectItems;
            return this;
        }

        public SelectQueryBuilder inSchema(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public SelectQueryBuilder from(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public SelectQueryBuilder tableAlias(String tableAlias) {
            this.tableAlias = tableAlias;
            return this;
        }

        public SelectQueryBuilder joins(ImmutableList<JoinItem> joins) {
            this.joins = joins;
            return this;
        }

        public SelectQueryBuilder join(JoinItem join) {
            this.joins = joins.append(join);
            return this;
        }

        public SelectQueryBuilder where(ImmutableList<WhereItem> where) {
            this.where = where;
            return this;
        }

        public SelectQueryBuilder orderBy(ImmutableList<OrderByItem> orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public SelectQueryBuilder limit(Object limit) {
            this.limit = limit;
            return this;
        }
        
        
        public SelectQuery build() {
            return new SelectQuery(this);
        }
        
    }
    
    
    public interface SelectItem {
        
    }
    
    
    public static class WildcardSelectItem implements SelectItem {
        
        private final String tableAlias;

        
        public WildcardSelectItem(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        
        public String tableAlias() {
            return tableAlias;
        }
        
    }
    
    
    public static class ExpressionSelectItem implements SelectItem {
        
        private final Expression expression;
        
        private final String alias;

        
        public ExpressionSelectItem(Expression expression, String alias) {
            this.expression = expression;
            this.alias = alias;
        }

        
        public Expression expression() {
            return expression;
        }
        
        public String alias() {
            return alias;
        }
        
    }
    
    
    public static class JoinItem {
        
        private final JoinType joinType;

        private final String targetSchemaName;
        
        private final String targetTableName;

        private final String targetTableAlias;
        
        private final String targetFieldName;

        private final String sourceTableAlias;

        private final String sourceFieldName;

        
        public JoinItem(
                JoinType joinType,
                String targetSchemaName,
                String targetTableName,
                String targetTableAlias,
                String targetFieldName,
                String sourceTableAlias,
                String sourceFieldName) {
            this.joinType = joinType;
            this.targetSchemaName = targetSchemaName;
            this.targetTableName = targetTableName;
            this.targetTableAlias = targetTableAlias;
            this.targetFieldName = targetFieldName;
            this.sourceTableAlias = sourceTableAlias;
            this.sourceFieldName = sourceFieldName;
        }


        public JoinType joinType() {
            return joinType;
        }

        public String targetSchemaName() {
            return targetSchemaName;
        }

        public String targetTableName() {
            return targetTableName;
        }

        public String targetTableAlias() {
            return targetTableAlias;
        }

        public String targetFieldName() {
            return targetFieldName;
        }

        public String sourceTableAlias() {
            return sourceTableAlias;
        }

        public String sourceFieldName() {
            return sourceFieldName;
        }

    }
    

    public static class WhereItem {

        private final String tableName;
        
        private final String fieldName;
        
        private final Object value;

        
        public WhereItem(String tableName, String fieldName, Object value) {
            this.tableName = tableName;
            this.fieldName = fieldName;
            this.value = value;
        }

        
        public String tableName() {
            return tableName;
        }

        public String fieldName() {
            return fieldName;
        }

        public Object value() {
            return value;
        }
        
    }
    

    public static class OrderByItem {

        private final String tableName;
        
        private final String fieldName;
        
        private final Integer position;
        
        private final boolean ascOrder;
        
        private final NullsOrderMode nullsOrderMode;

        public OrderByItem(
                String tableName, String fieldName, Integer position, boolean ascOrder, NullsOrderMode nullsOrderMode) {
            this.tableName = tableName;
            this.fieldName = fieldName;
            this.position = position;
            this.ascOrder = ascOrder;
            this.nullsOrderMode = nullsOrderMode;
        }

        
        public String tableName() {
            return tableName;
        }

        public String fieldName() {
            return fieldName;
        }

        public Integer position() {
            return position;
        }

        public boolean ascOrder() {
            return ascOrder;
        }

        public NullsOrderMode nullsOrderMode() {
            return nullsOrderMode;
        }
        
    }
    
}
