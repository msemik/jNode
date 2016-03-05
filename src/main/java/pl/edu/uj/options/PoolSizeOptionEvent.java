package pl.edu.uj.options;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class PoolSizeOptionEvent extends ApplicationEvent {
    private final int poolSize;

    public PoolSizeOptionEvent(int poolSize, Object source) {
        super(source);
        this.poolSize = poolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
