package hu.webarticum.minibase.storage.api;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface TableSelection extends Iterable<LargeInteger> {

    public boolean containsRow(LargeInteger rowIndex);
    
}
