package hu.webarticum.minibase.query.query;

import java.util.Objects;

import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.miniconnect.lang.ImmutableList;

public final class DeleteQuery implements Query {

    private final String schemaName;
    
    private final String tableName;
    
    private final ImmutableList<WhereItem> where;
    
    
    private DeleteQuery(DeleteQueryBuilder builder) {
        this.schemaName = builder.schemaName;
        this.tableName = Objects.requireNonNull(builder.tableName);
        this.where = builder.where;
    }
    
    public static DeleteQueryBuilder builder() {
        return new DeleteQueryBuilder();
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
    
    
    public static final class DeleteQueryBuilder {

        private String schemaName = null;
        
        private String tableName = null;
        
        private ImmutableList<WhereItem> where = ImmutableList.empty();

        
        private DeleteQueryBuilder() {
            // use builder()
        }
        
        
        public DeleteQueryBuilder inSchema(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public DeleteQueryBuilder from(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public DeleteQueryBuilder where(ImmutableList<WhereItem> where) {
            this.where = where;
            return this;
        }

        
        public DeleteQuery build() {
            return new DeleteQuery(this);
        }
        
    }
    
}
