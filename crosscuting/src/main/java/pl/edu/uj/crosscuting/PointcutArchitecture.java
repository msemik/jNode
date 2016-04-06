package pl.edu.uj.crosscuting;

import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by michal on 02.04.16. Dupa
 */
public class PointcutArchitecture {

    @Pointcut("execution(public * (@pl.edu.uj.crosscuting.LogInvocations *).*(..))")
    public void logClassPublicMethodsInvocations() {
    }

    @Pointcut("execution(@pl.edu.uj.crosscuting.LogInvocations * *(..))")
    public void logMethodInvocation() {
    }
}
