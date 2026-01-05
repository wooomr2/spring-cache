package woo.spring.cache.service.strategy.sharded;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import woo.spring.cache.service.strategy.split.SplitBloomFilter;
import woo.spring.cache.service.strategy.split.SplitBloomFilterRedisHandler;

import java.util.List;

@Component
@RequiredArgsConstructor
class SplitShardedBloomFilterRedisHandler {

    private final SplitBloomFilterRedisHandler splitBloomFilterRedisHandler;

    public void init(SplitShardedBloomFilter splitShardedBloomFilter) {
        List<SplitBloomFilter> shards = splitShardedBloomFilter.getShards();
        for (SplitBloomFilter shard : shards) {
            splitBloomFilterRedisHandler.init(shard);
        }
    }

    public void add(SplitShardedBloomFilter splitShardedBloomFilter, String value) {
        SplitBloomFilter shard = splitShardedBloomFilter.findShard(value);
        splitBloomFilterRedisHandler.add(shard, value);
    }

    public boolean mightContain(SplitShardedBloomFilter splitShardedBloomFilter, String value) {
        SplitBloomFilter shard = splitShardedBloomFilter.findShard(value);
        return splitBloomFilterRedisHandler.mightContain(shard, value);
    }

    public void delete(SplitShardedBloomFilter splitShardedBloomFilter) {
        List<SplitBloomFilter> shards = splitShardedBloomFilter.getShards();
        for (SplitBloomFilter shard : shards) {
            splitBloomFilterRedisHandler.delete(shard);
        }
    }
}
