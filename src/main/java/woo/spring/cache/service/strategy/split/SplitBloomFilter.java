package woo.spring.cache.service.strategy.split;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import woo.spring.cache.service.strategy.bloomfilter.BloomFilter;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitBloomFilter {

    private String id;
    private BloomFilter bloomFilter;
    private long splitCount;

    // public static final long BIT_SPLIT_UNIT = 1L << 32; // 2^32
    public static final long BIT_SPLIT_UNIT = 1L << 10; // 2^10 == 1024 for Test

    public static SplitBloomFilter create(String id, long dataCount, double falsePositiveRate) {
        BloomFilter bloomFilter = BloomFilter.create(id, dataCount, falsePositiveRate);

        // 비트 사이즈가 1024라면? ((1024-1) / 1024) + 1 == 1 1개의 Split
        // 비트 사이즈가 1025라면? ((1025-1) / 1024) + 1 == 2 2개의 Split
        long splitCount = ((bloomFilter.getBitSize() - 1) / BIT_SPLIT_UNIT) + 1;

        SplitBloomFilter splitBloomFilter = new SplitBloomFilter();
        splitBloomFilter.id = id;
        splitBloomFilter.bloomFilter = bloomFilter;
        splitBloomFilter.splitCount = splitCount;
        return splitBloomFilter;
    }

    public long findSplitIndex(long hashedIndex) {
        if (hashedIndex >= bloomFilter.getBitSize()) {
            throw new IllegalArgumentException("hashedIndex out of range");
        }

        // hashedIndex가 1023이라면 -> 0번쨰 Split
        // hashedIndex가 1024이라면 -> 1번쨰 Split
        return hashedIndex / BIT_SPLIT_UNIT;
    }

    public long calcSplitBitSize(long splitIndex) {
        if (splitIndex == splitCount - 1) {
            // bitSize(1025), splitCount(2), splitIndex(1)
            // 1025 - (1024 * 1) == 1
            return bloomFilter.getBitSize() - (BIT_SPLIT_UNIT * splitIndex);
        }
        return BIT_SPLIT_UNIT;
    }
}
