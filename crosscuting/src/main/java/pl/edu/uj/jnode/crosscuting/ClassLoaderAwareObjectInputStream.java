package pl.edu.uj.jnode.crosscuting;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

public class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
    private static final Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();
    private final ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param in          The <code>InputStream</code>.
     * @param classLoader classloader to use
     * @throws IOException if an I/O error occurs while reading stream header.
     * @see java.io.ObjectInputStream
     */
    public ClassLoaderAwareObjectInputStream(final InputStream in, final ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;

        primitiveTypes.put("byte", byte.class);
        primitiveTypes.put("short", short.class);
        primitiveTypes.put("int", int.class);
        primitiveTypes.put("long", long.class);
        primitiveTypes.put("float", float.class);
        primitiveTypes.put("double", double.class);
        primitiveTypes.put("boolean", boolean.class);
        primitiveTypes.put("char", char.class);
        primitiveTypes.put("void", void.class);
    }

    /**
     * Overriden version that uses the parametrized <code>ClassLoader</code> or the <code>ClassLoader</code>
     * of the current <code>Thread</code> to resolve the class.
     *
     * @param desc An instance of class <code>ObjectStreamClass</code>.
     * @return A <code>Class</code> object corresponding to <code>desc</code>.
     * @throws IOException            Any of the usual Input/Output exceptions.
     * @throws ClassNotFoundException If class of a serialized object cannot be found.
     */
    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        final String name = desc.getName();
        try {
            return Class.forName(name, false, classLoader);
        } catch (final ClassNotFoundException ex) {
            try {
                return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            } catch (final ClassNotFoundException cnfe) {
                final Class<?> cls = primitiveTypes.get(name);
                if (cls != null) {
                    return cls;
                }
                throw cnfe;
            }
        }
    }
}