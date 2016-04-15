package pl.edu.uj.jnode.cluster.callback;

import org.apache.commons.lang3.SerializationUtils;
import pl.edu.uj.jnode.crosscuting.ClassLoaderAwareObjectInputStream;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by alanhawrot on 08.04.2016.
 */
public class SerializableCallbackWrapper implements Serializable {
    private transient Callback callback;
    private transient byte[] serializedCallback;

    public SerializableCallbackWrapper(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void deserialize(Jar jar) {
        if (serializedCallback == null) {
            return;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedCallback);
            ClassLoaderAwareObjectInputStream stream = new ClassLoaderAwareObjectInputStream(inputStream, jar.getChildFirstClassLoader());
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
        if (serializedCallback != null) {
            out.writeInt(serializedCallback.length);
            out.write(serializedCallback);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int length = in.readInt();
        serializedCallback = new byte[length];
        in.readFully(serializedCallback);
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
