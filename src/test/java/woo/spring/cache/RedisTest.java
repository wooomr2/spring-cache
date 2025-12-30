package woo.spring.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisTest extends RedisTestContainerSupport {

    @Test
    void test1() {
        redisTemplate.opsForValue().set("mykey", "myvalue");
        String value = redisTemplate.opsForValue().get("mykey");
        System.out.println(value);
    }

    @Test
    void test2() {
        String value = redisTemplate.opsForValue().get("mykey");
        System.out.println(value);
    }

    @Test
    void test3() {
        String value = redisTemplate.opsForValue().get("mykey");
        System.out.println(value);
    }
}
