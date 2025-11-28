package woo.spring.cache.common.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final StringRedisTemplate redisTemplate;

    /**
     * (실습용) 실행 할 때마다 레디스 저장된 값 초기화
     */
    @PostConstruct
    public void clearRedisOnStartUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }
}
