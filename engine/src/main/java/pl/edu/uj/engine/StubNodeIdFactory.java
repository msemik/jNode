package pl.edu.uj.engine;

public class StubNodeIdFactory implements NodeIdFactory {
    @Override
    public String getCurrentNodeId() {
        return "CURRENT_NODE_ID";
    }
}
