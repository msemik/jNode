package pl.edu.uj.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.userlib.Callback;

/**
 * Created by michal on 22.11.15.
 */
public class EmptyCallback implements Callback {
    private Logger logger = LoggerFactory.getLogger(EmptyCallback.class);

    @Override
    public void onSuccess(Object taskResult) {
        logger.debug("Successful task execution, result: " + taskResult);
    }

    @Override
    public void onFailure(Throwable ex) {
        logger.debug("Unsuccessful task execution, exception: " + ex);
        ex.printStackTrace();
    }
}
