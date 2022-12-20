package hu.webarticum.minibase.query.expression;

import java.util.Objects;

public class ColumnParameter implements Parameter {

    private final String tableAlias;
    
    private final String columnName;
    
    
    public ColumnParameter(String tableAlias, String columnName) {
        this.tableAlias = tableAlias;
        this.columnName = columnName;
    }
    
    
    public String tableAlias() {
        return tableAlias;
    }
    
    public String columnName() {
        return columnName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof ColumnParameter)) {
            return false;
        }
        
        ColumnParameter otherColumnParameter = (ColumnParameter) other;
        return
                Objects.equals(tableAlias, otherColumnParameter.tableAlias) &&
                columnName.equals(otherColumnParameter.columnName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tableAlias, columnName);
    }
    
}
