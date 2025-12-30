package woo.spring.cache.service.strategy.bloomfilter;

import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BloomFilter {

    private String id;
    private long dataCount; // 데이터 수(n)
    private double falsePositiveRate; // 오차율(p)
    private long bitSize; // 비트수(m)
    private int hashFunctionCount; // 해시 함수 수(k)
    private List<BloomFilterHashFunction> hashFunctions;

    public static BloomFilter create(String id, long dataCount, double falsePositiveRate) {
        if (dataCount < 0) {
            throw new IllegalArgumentException("data count can't be negative");
        }

        if (falsePositiveRate <= 0.0 || falsePositiveRate >= 1.0) {
            throw new IllegalArgumentException("falsePositiveRate must be between 0.0 and 1.0");
        }

        long bitSize = calculateBitSize(dataCount, falsePositiveRate);
        int hashFunctionCount = calculateHashFunctionCount(dataCount, bitSize);

        List<BloomFilterHashFunction> hashFunctions = IntStream.range(0, hashFunctionCount)
                .mapToObj(seed ->
                        (BloomFilterHashFunction) v -> Math.abs(
                                Hashing.murmur3_128(seed).hashString(v, UTF_8).asLong() % bitSize
                        )
                ).toList();

        BloomFilter bloomFilter = new BloomFilter();
        bloomFilter.id = id;
        bloomFilter.dataCount = dataCount;
        bloomFilter.falsePositiveRate = falsePositiveRate;
        bloomFilter.bitSize = bitSize;
        bloomFilter.hashFunctionCount = hashFunctionCount;
        bloomFilter.hashFunctions = hashFunctions;
        return bloomFilter;
    }

    /**
     * -(n * ln(p)) / (ln(2))^2
     */
    private static long calculateBitSize(long dataCount, double falsePositiveRate) {
        return (long) Math.ceil(
                -(dataCount * Math.log(falsePositiveRate)) / (Math.pow(Math.log(2), 2))
        );
    }

    /**
     * (m / n) * ln(2)
     */
    private static int calculateHashFunctionCount(long dataCount, long bitSize) {
        return (int) Math.ceil(
                (bitSize / (double) dataCount) * Math.log(2)
        );
    }

    public List<Long> hash(String value) {
        return hashFunctions.stream()
                .map(f -> f.hash(value))
                .toList();
    }
}
