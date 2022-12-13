package hu.webarticum.minibase.query.parser;

import hu.webarticum.minibase.query.query.Query;

public interface SqlParser {

    public Query parse(String sql);
    
}
