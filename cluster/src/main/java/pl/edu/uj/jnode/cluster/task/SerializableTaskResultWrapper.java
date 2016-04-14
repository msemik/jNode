package pl.edu.uj.jnode.cluster.task;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.jnode.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.jnode.jarpath.Jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by alanhawrot on 14.04.2016.
 */
public class SerializableTaskResultWrapper implements Serializable {
    private transient Serializable taskResultOrException;
    private transient byte[] serializedTaskResultOrException;

    public SerializableTaskResultWrapper(Serializable taskResultOrException) {
        this.taskResultOrException = taskResultOrException;
    }

    public Serializable getTaskResultOrException() {
        if (taskResultOrException == null) {
            throw new IllegalStateException("Task result must be deserialized before getting it.");
        }
        return taskResultOrException;
    }

    private void setTaskResultOrException(Serializable taskResultOrException) {
        this.taskResultOrException = taskResultOrException;
    }

    public void deserialize(Jar jar) {
        if (serializedTaskResultOrException == null) {
            return;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedTaskResultOrException);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getChildFirstClassLoader());
            Object o = stream.readObject();
            setTaskResultOrException((Serializable) o);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        Serializable taskResultOrException = getTaskResultOrException();
        if (serializedTaskResultOrException == null && taskResultOrException != null) {
            this.serializedTaskResultOrException = SerializationUtils.serialize(taskResultOrException);
        }
        out.write(serializedTaskResultOrException);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int available = in.available();
        serializedTaskResultOrException = new byte[available];
        in.readFully(serializedTaskResultOrException);
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
