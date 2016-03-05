package pl.edu.uj.cluster;

import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jarpath.JarStateChangedEvent;

public interface JarDistributor {
    void on(ReceivedExternalTaskEvent event);

    void onJarRequest(String nodeId, String jarFileName);

    void onJarDelivery(String nodeId, String jarFileName, byte[] jar);

    void on(JarStateChangedEvent event);

    void on(CancelJarJobsEvent event);
}
