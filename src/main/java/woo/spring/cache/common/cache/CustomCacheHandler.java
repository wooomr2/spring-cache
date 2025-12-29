package woo.spring.cache.common.cache;

import java.time.Duration;
import java.util.function.Supplier;

public interface CustomCacheHandler {

    boolean supports(CacheStrategy cacheStrategy);

    <T> T fetch(String key, Duration ttl, Supplier<T> dataSourceSupplier, Class<T> clazz);

    void put(String key, Duration ttl, Object value);

    void evict(String key);
}
