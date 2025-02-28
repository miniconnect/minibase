package hu.webarticum.minibase.engine.facade;

import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.miniconnect.api.MiniSessionManager;

public class FrameworkSessionManager implements MiniSessionManager {
    
    private final Engine engine;
    

    public FrameworkSessionManager(Engine engine) {
        this.engine = engine;
    }
    
    
    @Override
    public FrameworkSession openSession() {
        return new FrameworkSession(engine.openSession());
    }

}
