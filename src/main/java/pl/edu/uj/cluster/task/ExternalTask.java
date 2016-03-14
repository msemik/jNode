package pl.edu.uj.cluster.task;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private transient JarFactory jarFactory;
    private transient WorkerPoolTask task;
    private transient byte[] serializedTask;
    private String sourceNodeId;
    private String jarName;

    public ExternalTask(WorkerPoolTask task, String sourceNodeId) {
        if (task == null)
            throw new IllegalStateException("Can't create null task!!");
        this.task = task;
        this.sourceNodeId = sourceNodeId;
        this.jarName = getJar().getFileName().toString();
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    @Override
    public Jar getJar() {
        return task.getJar();
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
    public boolean belongToJar(Jar jar) {
        if (task != null)
            return task.belongToJar(jar);
        return jarFactory.getFor(sourceNodeId, jarName).equals(jar);
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

    public void deserialize(Jar jar) {
        if (serializedTask == null)
            return;

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedTask);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getClassLoader());
            Object o = stream.readObject();
            if (!(o instanceof ExternalTask))
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
