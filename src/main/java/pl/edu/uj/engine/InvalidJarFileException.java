package pl.edu.uj.engine;

/**
 * Created by michal on 22.11.15.
 */
public class InvalidJarFileException extends RuntimeException {
    public InvalidJarFileException(Exception e) {
        super(e);
    }

    public InvalidJarFileException(String message, Exception e) {
        super(message, e);
    }

    public InvalidJarFileException(String s) {
        super(s);
    }
}
