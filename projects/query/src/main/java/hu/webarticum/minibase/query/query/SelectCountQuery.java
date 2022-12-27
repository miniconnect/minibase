package hu.webarticum.minibase.query.query;

import java.util.Objects;

import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.miniconnect.lang.ImmutableList;

public final class SelectCountQuery implements Query {
    
    private final String schemaName;

    private final String tableName;
    
    private final ImmutableList<WhereItem> where;
    
    
    private SelectCountQuery(SelectCountQueryBuilder builder) {
        this.schemaName = builder.schemaName;
        this.tableName = Objects.requireNonNull(builder.tableName);
        this.where = builder.where;
    }
    
    public static SelectCountQueryBuilder builder() {
        return new SelectCountQueryBuilder();
    }
    

    public String schemaName() {
        return schemaName;
    }

    public String tableName() {
        return tableName;
    }

    public ImmutableList<WhereItem> where() {
        return where;
    }
    
    
    public static final class SelectCountQueryBuilder {
        
        private String schemaName = null;

        private String tableName = null;
        
        private ImmutableList<WhereItem> where = ImmutableList.empty();
        
        
        private SelectCountQueryBuilder() {
            // use builder()
        }
        
        
        public SelectCountQueryBuilder inSchema(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public SelectCountQueryBuilder from(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public SelectCountQueryBuilder where(ImmutableList<WhereItem> where) {
            this.where = where;
            return this;
        }

        
        public SelectCountQuery build() {
            return new SelectCountQuery(this);
        }
        
    }
    
}
