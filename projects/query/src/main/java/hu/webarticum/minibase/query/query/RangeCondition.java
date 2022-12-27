package hu.webarticum.minibase.query.query;

public class RangeCondition implements SpecialCondition {

    private final Object from;
    
    private final boolean fromInclusive;

    private final Object to;
    
    private final boolean toInclusive;
    
    
    public RangeCondition(Object from, boolean fromInclusive, Object to, boolean toInclusive) {
        this.from = from;
        this.fromInclusive = fromInclusive;
        this.to = to;
        this.toInclusive = toInclusive;
    }
    
    
    public Object from() {
        return from;
    }
    
    public boolean fromInclusive() {
        return fromInclusive;
    }
    
    public Object to() {
        return to;
    }
    
    public boolean toInclusive() {
        return toInclusive;
    }
    
}
