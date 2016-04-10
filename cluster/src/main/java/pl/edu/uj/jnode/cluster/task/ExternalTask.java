package pl.edu.uj.jnode.cluster.task;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.jnode.cluster.node.Node;
import pl.edu.uj.jnode.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.jnode.engine.workerpool.BaseWorkerPoolTask;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class ExternalTask extends WorkerPoolTaskDecorator {
    private transient byte[] serializedTask;
    private String sourceNodeId;
    private String jarName;
    private int priority;
    private long taskId;

    public ExternalTask(WorkerPoolTask task, String sourceNodeId) {
        super(task);
        this.sourceNodeId = sourceNodeId;
        this.jarName = getJar().getFileName().toString();
        this.priority = task.getPriority();
        this.taskId = task.getTaskId();
    }

    @Override
    public Jar getJar() {
        WorkerPoolTask task = getTask();
        if (task == null) {
            throw new IllegalStateException("Jar must be deserialized before getting it.");
        }
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
        this.priority++;
        super.incrementPriority();
    }

    public String getSourceNodeId() {
        return sourceNodeId;
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

    public String getJarName() {
        return jarName;
    }

    public void deserialize(Jar jar) {
        if (serializedTask == null) {
            return;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedTask);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getChildFirstClassLoader());
            Object o = stream.readObject();
            if (!(o instanceof WorkerPoolTask)) {
                throw new IllegalStateException("Invalid task class: " + o.getClass().getSimpleName());
            }
            if (o instanceof BaseWorkerPoolTask) {
                ((BaseWorkerPoolTask) o).setJar(jar);
            }

            setTask((WorkerPoolTask) o);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        WorkerPoolTask task = getTask();
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
