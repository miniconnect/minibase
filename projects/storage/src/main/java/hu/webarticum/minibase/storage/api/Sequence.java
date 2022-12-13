package hu.webarticum.minibase.storage.api;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Sequence {
    
    public LargeInteger get();
    
    public LargeInteger getAndIncrement();
    
    public void ensureGreaterThan(LargeInteger high);

}
