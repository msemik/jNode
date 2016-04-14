package pl.edu.uj.jnode.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.jnode.userlib.Callback;

import java.io.Serializable;

/**
 * Created by michal on 22.11.15.
 */
public class EmptyCallback implements Callback {
    public final static EmptyCallback INSTANCE = new EmptyCallback();
    private Logger logger = LoggerFactory.getLogger(EmptyCallback.class);

    private EmptyCallback() {
    }

    @Override
    public void onSuccess(Serializable taskResult) {
        logger.debug("Successful task execution, result: " + taskResult);
    }

    @Override
    public void onFailure(Throwable ex) {
        logger.debug("Unsuccessful task execution, exception: " + ex);
        ex.printStackTrace();
    }
}
