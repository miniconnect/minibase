package hu.webarticum.minibase.query.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class StandaloneSelectQuery implements Query {

    private final ImmutableList<String> aliases;

    private final ImmutableList<ImmutableList<Expression>> expressionMatrix;
    
    
    private StandaloneSelectQuery(StandaloneSelectQueryBuilder builder) {
        this.aliases = Objects.requireNonNull(builder.aliases);
        this.expressionMatrix = ImmutableList.fromCollection(builder.expressionMatrix);
    }
    
    public static StandaloneSelectQueryBuilder builder() {
        return new StandaloneSelectQueryBuilder();
    }


    public ImmutableList<String> aliases() {
        return aliases;
    }
    
    public ImmutableList<ImmutableList<Expression>> expressionMatrix() {
        return expressionMatrix;
    }

    
    public static final class StandaloneSelectQueryBuilder {

        private ImmutableList<String> aliases = null;

        private List<ImmutableList<Expression>> expressionMatrix = new ArrayList<>();
        
        
        private StandaloneSelectQueryBuilder() {
            // use builder()
        }
        

        public StandaloneSelectQueryBuilder aliases(Iterable<String> aliases) {
            this.aliases = ImmutableList.fromIterable(aliases);
            return this;
        }
        
        public StandaloneSelectQueryBuilder expressionMatrix(
                Iterable<? extends Iterable<Expression>> expressionMatrix) {
            this.expressionMatrix.clear();
            for (Iterable<Expression> expressionRow : expressionMatrix) {
                this.expressionMatrix.add(ImmutableList.fromIterable(expressionRow));
            }
            return this;
        }
        
        public StandaloneSelectQueryBuilder addExpressionRow(Iterable<Expression> expressionRow) {
            expressionMatrix.add(ImmutableList.fromIterable(expressionRow));
            return this;
        }

        
        public StandaloneSelectQuery build() {
            return new StandaloneSelectQuery(this);
        }
        
    }
    
}
