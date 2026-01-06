package woo.spring.cache.common.cache;

public enum CacheStrategy {
    NONE,
    SPRING_CACHE_ANNOTATION,
    NULL_OBJECT_PATTERN,
    BLOOM_FILTER,
    SPLIT_BLOOM_FILTER,
    SPLIT_SHADED_BLOOM_FILTER,
    SPLIT_SHARDED_SUB_BLOOM_FLITER,
}
