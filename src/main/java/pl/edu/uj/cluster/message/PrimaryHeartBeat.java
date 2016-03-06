package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;


public class PrimaryHeartBeat implements Serializable, Distributable {
    private long poolSize;
    private long jobsInPool;
    private String expectedHeartBeatType; //PRIMARY|EXTENDED

    public PrimaryHeartBeat(long poolSize, long jobsInPool, String expectedHeartBeatType) {
        this.poolSize = poolSize;
        this.jobsInPool = jobsInPool;
        this.expectedHeartBeatType = expectedHeartBeatType;
    }

    public long getPoolSize() {
        return poolSize;
    }

    public long getJobsInPool() {
        return jobsInPool;
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
                "poolSize=" + poolSize +
                ", jobsInPool=" + jobsInPool +
                ", expectedHeartBeatType='" + expectedHeartBeatType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryHeartBeat that = (PrimaryHeartBeat) o;

        if (poolSize != that.poolSize) return false;
        if (jobsInPool != that.jobsInPool) return false;
        return expectedHeartBeatType != null ? expectedHeartBeatType.equals(that.expectedHeartBeatType) : that.expectedHeartBeatType == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (poolSize ^ (poolSize >>> 32));
        result = 31 * result + (int) (jobsInPool ^ (jobsInPool >>> 32));
        result = 31 * result + (expectedHeartBeatType != null ? expectedHeartBeatType.hashCode() : 0);
        return result;
    }

    public static PrimaryHeartBeat empty() {
        return new PrimaryHeartBeat(0, 0, null);
    }

    public static PrimaryHeartBeat create(long poolSize, long jobsInPool) {
        return new PrimaryHeartBeat(poolSize, jobsInPool, null);
    }

}
