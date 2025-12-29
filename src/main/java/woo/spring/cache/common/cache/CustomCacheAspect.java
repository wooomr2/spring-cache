package woo.spring.cache.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CustomCacheAspect {

    private final List<CustomCacheHandler> customCacheHandlers;
    private final CustomCacheKeyGenerator customCacheKeyGenerator;

    @Around("@annotation(customCacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, CustomCacheable customCacheable) {
        CacheStrategy cacheStrategy = customCacheable.cacheStrategy();
        CustomCacheHandler cacheHandler = findCacheHandler(cacheStrategy);

        String key = customCacheKeyGenerator.genKey(joinPoint, cacheStrategy, customCacheable.cacheName(), customCacheable.key());
        Duration ttl = Duration.ofSeconds(customCacheable.ttlSeconds());
        Supplier<Object> dataSourceSupplier = createDataSourceSupplier(joinPoint);
        Class returnType = findReturnType(joinPoint);

        try {
            log.info("[CustomCacheAspect.handleCacheable] key={}", key);
            return cacheHandler.fetch(
                    key,
                    ttl,
                    dataSourceSupplier,
                    returnType
            );
        } catch (Exception e) {
            log.error("[CustomCacheAspect.handleCacheable] key={}", key, e);
            return dataSourceSupplier.get();
        }
    }

    private CustomCacheHandler findCacheHandler(CacheStrategy cacheStrategy) {
        return customCacheHandlers.stream()
                .filter(hadnler -> hadnler.supports(cacheStrategy))
                .findFirst()
                .orElseThrow();
    }

    private Supplier<Object> createDataSourceSupplier(ProceedingJoinPoint joinPoint) {

        return () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Class findReturnType(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }

    @AfterReturning(pointcut = "@annotation(customCachePut)", returning = "result")
    public void handleCachePut(JoinPoint joinPoint, CustomCachePut customCachePut, Object result) {
        CacheStrategy cacheStrategy = customCachePut.cacheStrategy();
        CustomCacheHandler cacheHandler = findCacheHandler(cacheStrategy);

        String key = customCacheKeyGenerator.genKey(joinPoint, cacheStrategy, customCachePut.cacheName(), customCachePut.key());
        log.info("[CustomCacheAspect.handleCachePut] key={}", key);
        cacheHandler.put(key, Duration.ofSeconds(customCachePut.ttlSeconds()), result);
    }

    @AfterReturning(pointcut = "@annotation(customCacheEvict)")
    public void handleCacheEvict(JoinPoint joinPoint, CustomCacheEvict customCacheEvict) {
        CacheStrategy cacheStrategy = customCacheEvict.cacheStrategy();
        CustomCacheHandler cacheHandler = findCacheHandler(cacheStrategy);

        String key = customCacheKeyGenerator.genKey(joinPoint, cacheStrategy, customCacheEvict.cacheName(), customCacheEvict.key());
        log.info("[CustomCacheAspect.handleCacheEvict] key={}", key);
        cacheHandler.evict(key);
    }
}
