package pl.edu.uj.crosscuting;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class ReflectionUtils {
    public static Object readFieldValue(Object object, String fieldName) {
        try {
            return FieldUtils.readDeclaredField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            //shouldn't happen according to readDeclaredField with forceAccess set to true.
            throw new AssertionError(e);
        }
    }

    public static Object readFieldValue(Class<?> cls, Object object, String fieldName) {
        try {
            final Field field = FieldUtils.getDeclaredField(cls, fieldName, true);
            if (field == null) {
                throw new AssertionError("Invalid field name" + fieldName + " for class " + cls);
            }
            return FieldUtils.readField(field, object, false);
        } catch (IllegalAccessException e) {
            //shouldn't happen according to readDeclaredField with forceAccess set to true.
            throw new AssertionError(e);
        }
    }
}
