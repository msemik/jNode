package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class PrimaryHeartBeat implements Serializable
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
}
