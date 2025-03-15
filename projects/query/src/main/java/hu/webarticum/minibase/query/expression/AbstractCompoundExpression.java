package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Set;

import hu.webarticum.miniconnect.lang.ImmutableList;

public abstract class AbstractCompoundExpression implements Expression {

    protected ImmutableList<Expression> subExpressions;
    
    
    protected AbstractCompoundExpression(ImmutableList<Expression> subExpressions) {
        this.subExpressions = subExpressions;
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression subExpression : subExpressions) {
            subParameters.addAll(subExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

}
