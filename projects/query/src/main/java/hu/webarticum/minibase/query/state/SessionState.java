package hu.webarticum.minibase.query.state;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface SessionState {

    public String getCurrentSchema();

    public void setCurrentSchema(String schemaName);

    public LargeInteger getLastInsertId();

    public void setLastInsertId(LargeInteger lastInsertId);

    public Object getUserVariable(String variableName);

    public void setUserVariable(String variableName, Object value);

}
