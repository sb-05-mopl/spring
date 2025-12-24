package com.mopl.moplwebsocketsse.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

	@Value("${logging.aspect.slow-method-threshold:3000}")
	private long slowMethodThreshold;

	@Around("execution(* com.mopl.moplwebsocketsse.domain..controller..*(..)) || " +
		"execution(* com.mopl.moplwebsocketsse.domain..service..*(..))")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		String signature = getMethodSignature(joinPoint);

		log.debug("[LoggingAspect-ws] START - {}.{}({})", className, methodName, signature);

		long startTime = System.currentTimeMillis();

		try {
			Object result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - startTime;

			if (executionTime > slowMethodThreshold) {
				log.warn("[LoggingAspect-ws] SLOW - {}.{} | {}ms - 실행 시간이 {}ms보다 느립니다",
					className, methodName, executionTime, slowMethodThreshold);
			} else {
				log.debug("[LoggingAspect-ws] END - {}.{} | {}ms",
					className, methodName, executionTime);
			}

			return result;

		} catch (Throwable e) {
			long executionTime = System.currentTimeMillis() - startTime;
			log.error("[LoggingAspect-ws] ERROR - {}.{} | {}ms - 실행 중 예외 발생: {}: {}",
				className, methodName, executionTime,
				e.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	private String getMethodSignature(ProceedingJoinPoint joinPoint) {
		return Arrays.stream(joinPoint.getArgs())
			.map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
			.collect(Collectors.joining(", "));
	}
}