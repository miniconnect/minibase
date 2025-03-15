package hu.webarticum.minibase.storage.impl.simple;

import java.util.concurrent.atomic.AtomicReference;

import hu.webarticum.minibase.storage.api.Sequence;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SimpleSequence implements Sequence {
    
    private final AtomicReference<LargeInteger> valueHolder;
    
    
    public SimpleSequence() {
        this(LargeInteger.ONE);
    }

    public SimpleSequence(LargeInteger value) {
        this.valueHolder = new AtomicReference<>(value);
    }
    

    @Override
    public LargeInteger get() {
        return valueHolder.get();
    }

    @Override
    public LargeInteger getAndIncrement() {
        return valueHolder.getAndUpdate(v -> v.add(LargeInteger.ONE));
    }

    @Override
    public void ensureGreaterThan(LargeInteger high) {
        valueHolder.updateAndGet(v -> calculateForEnsureGreaterThan(v, high));
    }
    
    private LargeInteger calculateForEnsureGreaterThan(LargeInteger currentValue, LargeInteger high) {
        if (currentValue.compareTo(high) > 0) {
            return currentValue;
        }
        
        return high.add(LargeInteger.ONE);
    }

}
