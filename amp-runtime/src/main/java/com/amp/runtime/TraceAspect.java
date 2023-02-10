package com.amp.runtime;

import android.os.Looper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TraceAspect {

    @Pointcut("execution(* *(..))")
    private void anyOperation() {}

    @Pointcut("execution(* com.amp.sample.app..*.*(..))")
    private void inApplicationPackage() {}

    @Pointcut("execution(* com.amp.app.featuremodule..*.*(..))")
    private void inFeatureModule() {}

    @Pointcut("anyOperation() && (inApplicationPackage() || inFeatureModule())")
    private void execute() {}


    @Around("execute()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();
            boolean isMain = (Looper.myLooper() == Looper.getMainLooper());

            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();

            DebugLog.log(className, buildLogMessage(isMain? "Main thread" : "Background Thread", className, methodName, stopWatch.getTotalTimeMillis()));
            return result;
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

    /**
     * Create a log message.
     *
     *
     *
     * @param threadInfo Thread details
     * @param className Name of the containing class
     * @param methodName A string with the method name.
     * @param methodDuration Duration of the method in milliseconds.
     * @return A string representing message.
     */
    private static String buildLogMessage(String threadInfo, String className, String methodName, long methodDuration) {
        StringBuilder message = new StringBuilder();
        message.append("Time Consumed for --> ");
        message.append(className);
        message.append(" in "+threadInfo +" ");
        message.append(methodName);
        message.append(" --> ");
        message.append("[");
        message.append(methodDuration);
        message.append("ms");
        message.append("]");

        return message.toString();
    }
}