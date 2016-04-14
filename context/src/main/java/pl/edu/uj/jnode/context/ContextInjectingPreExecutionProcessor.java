package pl.edu.uj.jnode.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.CallbackPreExecutionProcessor;
import pl.edu.uj.jnode.engine.TaskPreExecutionProcessor;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;
import pl.edu.uj.jnode.userlib.Task;

@Component
public class ContextInjectingPreExecutionProcessor implements CallbackPreExecutionProcessor, TaskPreExecutionProcessor {
    @Autowired
    private JarContextRegistry jarContextRegistry;

    @Override
    public Callback process(Jar jar, Callback callback) {
        JarContext context = jarContextRegistry.get(jar);
        context.injectContext(callback);
        return callback;
    }

    @Override
    public void process(WorkerPoolTask task) {
        Jar jar = task.getJar();
        JarContext jarContext = jarContextRegistry.get(jar);
        Task rawTask = task.getRawTask();
        jarContext.injectContext(rawTask);
    }
}
