package woo.spring.cache.service.strategy.sharded;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import woo.spring.cache.service.strategy.split.SplitBloomFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplitShardedBloomFilterTest {

    private static final Logger log = LoggerFactory.getLogger(SplitShardedBloomFilterTest.class);

    @Test
    void crate() {
        SplitShardedBloomFilter splitShardedBloomFilter = SplitShardedBloomFilter.create("testId", 1000, 0.01, 4);

        long dataCount = 0;
        List<SplitBloomFilter> shards = splitShardedBloomFilter.getShards();
        for (SplitBloomFilter shard : shards) {
            System.out.println("shard: " + shard);
            dataCount += shard.getBloomFilter().getDataCount();
        }

        assertThat(dataCount).isEqualTo(splitShardedBloomFilter.getDataCount());
    }

    @Test
    void findShard() {
        SplitShardedBloomFilter splitShardedBloomFilter = SplitShardedBloomFilter.create("testId", 1000, 0.01, 4);

        SplitBloomFilter shard = splitShardedBloomFilter.findShard("value");
        System.out.println("shard: " + shard);
        assertThat(shard).isNotNull();
    }
}