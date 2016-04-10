package pl.edu.uj.jnode.crosscuting;

import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by michal on 02.04.16. Dupa
 */
public class PointcutArchitecture {
    @Pointcut("execution(public * (@pl.edu.uj.jnode.crosscuting.LogInvocations *).*(..))")
    public void logClassPublicMethodsInvocations() {
    }

    @Pointcut("execution(@pl.edu.uj.jnode.crosscuting.LogInvocations * *(..))")
    public void logMethodInvocation() {
    }
}
