package pl.edu.uj.cluster.task;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.jarpath.JarFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class ExternalTask implements WorkerPoolTask {
    private transient WorkerPoolTask task;
    private transient byte[] serializedTask;
    private transient Jar jar;
    private String sourceNodeId;
    private String jarName;
    private int priority;
    private long taskId;

    public ExternalTask(WorkerPoolTask task, String sourceNodeId) {
        if (task == null)
            throw new IllegalStateException("Can't create with null task!!");
        this.task = task;
        this.sourceNodeId = sourceNodeId;
        this.jarName = getJar().getFileName().toString();
        this.priority = task.getPriority();
        this.taskId = task.getTaskId();
    }

    @Override
    public Jar getJar() {
        if (jar != null)
            return jar;
        return task.getJar();
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @Override
    public boolean isExternal() {
        return true;
    }

    @Override
    public boolean belongToJar(Jar jar) {
        return getJar().equals(jar);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void incrementPriority() {
        task.incrementPriority();
    }

    public void populateJar(JarFactory jarFactory) {
        this.jar = jarFactory.getFor(sourceNodeId, jarName);
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }

    @Override
    public String toString() {
        return "ExternalTask{" +
                "task=" + taskId +
                ", sourceNodeId='" + sourceNodeId + '\'' +
                '}';
    }

    public boolean isOriginatedAt(Node selectedNode) {
        return sourceNodeId.equals(selectedNode.getNodeId());
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public void deserialize(Jar jar) {
        if (serializedTask == null)
            return;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedTask);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getClassLoader());
            Object o = stream.readObject();
            if (!(o instanceof WorkerPoolTask))
                throw new IllegalStateException("Invalid task class: " + o.getClass().getSimpleName());
            this.task = (WorkerPoolTask) o;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (serializedTask == null && task != null) {
            this.serializedTask = SerializationUtils.serialize(task);
        }

        out.write(serializedTask);
        //System.out.println("writeObject task size: " + serializedTask.length);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int available = in.available();
        //System.out.println("readObject task size: " + available);
        serializedTask = new byte[available];
        in.readFully(serializedTask);
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
