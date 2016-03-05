package pl.edu.uj.crosscuting;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;

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

}
