package pl.edu.uj.jnode.crosscuting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by michal on 02.04.16. Dupa
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LogInvocations {
}
