package pl.edu.uj.cluster.task;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class ExternalTask implements WorkerPoolTask {
    private transient WorkerPoolTask task;
    private transient byte[] serializedTask;
    private String sourceNodeId;
    private String jarName;

    public ExternalTask(WorkerPoolTask task, String sourceNodeId) {
        if (task == null)
            throw new IllegalStateException("Can't create null task!!");
        this.task = task;
        this.sourceNodeId = sourceNodeId;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    @Override
    public Path getJarName() {
        if (jarName != null) {
            return Paths.get(jarName);
        }
        return task.getJarName();
    }

    @Override
    public long getTaskId() {
        return task.getTaskId();
    }

    @Override
    public boolean isExternal() {
        return true;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }

    @Override
    public String toString() {
        return "ExternalTask{" +
                "task=" + task +
                ", sourceNodeId='" + sourceNodeId + '\'' +
                '}';
    }

    public boolean isOriginatedAt(Node selectedNode) {
        return sourceNodeId.equals(selectedNode.getNodeId());
    }

    @Override
    public boolean belongToJar(Path jarFileName) {
        if (task != null)
            return task.belongToJar(jarFileName);
        return getJarName().equals(jarFileName);
    }

    @Override
    public int getPriority() {
        return task.getPriority();
    }

    @Override
    public void incrementPriority() {
        task.incrementPriority();
    }


    public boolean isDeserialized() {
        return getTask() != null;
    }

    public void deserialize(ClassLoader classLoader) throws IOException, ClassNotFoundException {
        if (serializedTask == null)
            return;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedTask);
        ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, classLoader);
        Object o = stream.readObject();
        if (!(o instanceof ExternalTask))
            throw new IllegalStateException("Invalid task class: " + o.getClass().getSimpleName());
        this.task = (WorkerPoolTask) o;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (serializedTask == null && task != null) {
            this.serializedTask = SerializationUtils.serialize(task);
        }

        out.writeObject(getJarName().toString());
        out.write(serializedTask);
        //System.out.println("writeObject task size: " + serializedTask.length);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.jarName = (String) in.readObject();
        int available = in.available();
        //System.out.println("readObject task size: " + available);
        serializedTask = new byte[available];
        in.readFully(serializedTask);
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
