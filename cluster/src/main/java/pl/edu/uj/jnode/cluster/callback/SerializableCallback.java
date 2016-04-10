package pl.edu.uj.jnode.cluster.callback;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.jnode.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;

/**
 * Created by alanhawrot on 08.04.2016.
 */
public class SerializableCallback implements Callback {
    private transient Callback callback;
    private transient byte[] serializedCallback;

    public SerializableCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(Object taskResult) {
        if (callback != null) {
            callback.onSuccess(taskResult);
        } else {
            throw new IllegalStateException("Callback must be deserialized before executing it's method");
        }
    }

    @Override
    public void onFailure(Throwable ex) {
        if (callback != null) {
            callback.onFailure(ex);
        } else {
            throw new IllegalStateException("Callback must be deserialized before executing it's method");
        }
    }

    public void deserialize(Jar jar) {
        if (serializedCallback == null) {
            return;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedCallback);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getClassLoader());
            Object o = stream.readObject();
            setCallback((Callback) o);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        Callback callback = getCallback();
        if (serializedCallback == null && callback != null) {
            this.serializedCallback = SerializationUtils.serialize(callback);
        }
        out.write(serializedCallback);
    }

    private Callback getCallback() {
        return callback;
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int available = in.available();
        serializedCallback = new byte[available];
        in.readFully(serializedCallback);
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
