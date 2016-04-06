package pl.edu.uj.crosscuting;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
public class InvocationLogger {
    @Before("PointcutArchitecture.logClassPublicMethodsInvocations() || PointcutArchitecture.logMethodInvocation()")
    public void onEntry(JoinPoint joinPoint) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.debug("enter " + getPrintableMethodSignature(joinPoint));
    }

    private String getPrintableMethodSignature(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String args = getConcatenatedArgsClassNames(joinPoint);
        return signature.getName() + "(" + args + ")";
    }

    private String getConcatenatedArgsClassNames(JoinPoint joinPoint) {
        return Stream.of(joinPoint.getArgs())
                .map(o -> o.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }

    @After("PointcutArchitecture.logClassPublicMethodsInvocations() || PointcutArchitecture.logMethodInvocation()")
    public void onExit(JoinPoint joinPoint) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.debug("exit " + getPrintableMethodSignature(joinPoint));
    }
}
