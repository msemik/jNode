package pl.edu.uj.jnode.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.CallbackPreExecutionProcessor;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.userlib.Callback;

@Component
public class ContextInjectingCallbackPreExecutionProcessor implements CallbackPreExecutionProcessor {
    @Autowired
    private JarContextRegistry jarContextRegistry;

    @Override
    public Callback process(Jar jar, Callback callback) {
        JarContext context = jarContextRegistry.get(jar);
        context.injectContext(callback);
        return callback;
    }
}
