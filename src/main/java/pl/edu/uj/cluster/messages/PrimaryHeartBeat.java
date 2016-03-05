package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;
import java.io.Serializable;
import java.util.Optional;


public class PrimaryHeartBeat implements Serializable, Distributable
{
    private long threadsInPool;
    private long threadsInUse;
    private String expectedHeartBeatType; //PRIMARY|EXTENDED

    public PrimaryHeartBeat(long threadsInPool, long threadsInUse, String expectedHeartBeatType) {
        this.threadsInPool = threadsInPool;
        this.threadsInUse = threadsInUse;
        this.expectedHeartBeatType = expectedHeartBeatType;
    }

    public long getThreadsInPool() {
        return threadsInPool;
    }

    public long getThreadsInUse() {
        return threadsInUse;
    }

    public String getExpectedHeartBeatType() {
        return expectedHeartBeatType;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onPrimaryHeartBeat(sourceNodeId, this);
    }

    @Override
    public String toString() {
        return "PrimaryHeartBeat{" +
                "threadsInPool=" + threadsInPool +
                ", threadsInUse=" + threadsInUse +
                ", expectedHeartBeatType='" + expectedHeartBeatType + '\'' +
                '}';
    }
}
