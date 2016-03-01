package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class JarDelivery implements Serializable {
    private byte[] jar;
    private String fileName;

    public JarDelivery(byte[] jar, String fileName) {
        this.jar = jar;
        this.fileName = fileName;
    }

    public byte[] getJar() {
        return jar;
    }

    public String getFileName() {
        return fileName;
    }
}
