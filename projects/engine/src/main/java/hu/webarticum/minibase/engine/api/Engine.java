package hu.webarticum.minibase.engine.api;

import hu.webarticum.miniconnect.lang.CheckableCloseable;

public interface Engine extends CheckableCloseable {

    public EngineSession openSession();

}
