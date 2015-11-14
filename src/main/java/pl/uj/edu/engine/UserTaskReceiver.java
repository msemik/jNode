package pl.uj.edu.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

/**
 * This is a version of class UserTaskReceiver loaded by jNode.
 * <p>
 * User should have a version without any dependencies and with
 * one empty (without any implementation) method:
 * public void doAsync(Task task, Callback callback) {}
 * <p>
 * Because build-time weaving is enabled, UserTaskReceiver instances
 * created with 'new' keyword in user's code will be treated
 * as beans in jNode, so autowiring will work.
 *
 * @author alanhawrot
 */
@Configurable
public class UserTaskReceiver {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void doAsync(Task task, Callback callback) {
        eventPublisher.publishEvent(new NewTaskCreatedEvent(this, task, callback));
    }
}
