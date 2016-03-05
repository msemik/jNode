package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;


public class PrimaryHeartBeat implements Serializable, Distributable {
    private long jobsInPool;
    private long threadsInUse;
    private String expectedHeartBeatType; //PRIMARY|EXTENDED

    public PrimaryHeartBeat(long jobsInPool, long threadsInUse, String expectedHeartBeatType) {
        this.jobsInPool = jobsInPool;
        this.threadsInUse = threadsInUse;
        this.expectedHeartBeatType = expectedHeartBeatType;
    }

    public long getJobsInPool() {
        return jobsInPool;
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
                "jobsInPool=" + jobsInPool +
                ", threadsInUse=" + threadsInUse +
                ", expectedHeartBeatType='" + expectedHeartBeatType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryHeartBeat that = (PrimaryHeartBeat) o;

        if (jobsInPool != that.jobsInPool) return false;
        if (threadsInUse != that.threadsInUse) return false;
        return expectedHeartBeatType != null ? expectedHeartBeatType.equals(that.expectedHeartBeatType) : that.expectedHeartBeatType == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (jobsInPool ^ (jobsInPool >>> 32));
        result = 31 * result + (int) (threadsInUse ^ (threadsInUse >>> 32));
        result = 31 * result + (expectedHeartBeatType != null ? expectedHeartBeatType.hashCode() : 0);
        return result;
    }

    public static PrimaryHeartBeat empty() {
        return new PrimaryHeartBeat(0, 0, null);
    }

    public static PrimaryHeartBeat create(long threadsInUse, long threadsInPool) {
        return new PrimaryHeartBeat(threadsInUse, threadsInPool, null);
    }

}
