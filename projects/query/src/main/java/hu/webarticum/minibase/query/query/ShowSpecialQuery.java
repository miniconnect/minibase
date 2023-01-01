package hu.webarticum.minibase.query.query;

import java.util.Objects;

import hu.webarticum.minibase.query.expression.SpecialValueExpression;

public class ShowSpecialQuery implements Query {

    private final SpecialValueExpression specialValueExpression;

    private final String alias;
    
    
    private ShowSpecialQuery(ShowSpecialQueryBuilder builder) {
        this.specialValueExpression = Objects.requireNonNull(builder.specialValueExpression);
        this.alias = builder.alias;
    }
    
    public static ShowSpecialQueryBuilder builder() {
        return new ShowSpecialQueryBuilder();
    }
    
    
    public SpecialValueExpression specialValueExpression() {
        return specialValueExpression;
    }

    public String alias() {
        return alias;
    }

    
    public static final class ShowSpecialQueryBuilder {
        
        private SpecialValueExpression specialValueExpression = null;
        
        private String alias = null;

        
        private ShowSpecialQueryBuilder() {
            // use builder()
        }
        

        public ShowSpecialQueryBuilder specialValueExpression(SpecialValueExpression specialValueExpression) {
            this.specialValueExpression = specialValueExpression;
            return this;
        }

        public ShowSpecialQueryBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        
        public ShowSpecialQuery build() {
            return new ShowSpecialQuery(this);
        }
        
    }
    
}
