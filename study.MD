* Bloom Filter
*
* [한계]
* BloomFilter는 입력되는 데이터 수가 많아질수록 오차율이 증가한다.(입력된 데이터를 삭제할 수 도 없다)
* 초기에 n개의 데이터 입력을 가정했다면, n개를 초과하는 시점부터 오차율이 점점 증가하게 된다.
* 이미 만들어진 BloomFilter는 도중에 크기를 늘릴수도 없다(이미 고정된 크기범위에 대해 해시 함수가 동작하기 떄문)
* -> 처음부터 BloomFilter를 크게 만든다(but Redis 메모리제한, 초기에 불필요하게 큰 메모리를 할당해야함)
* -> 그럼 다른 방안은? (Split / Sharding / Sub-Filter)

Bloom Filter - Split

- 블룸필터 분할 전략
- Redis 비트맵의 최대 크기인 2^32 비트의 필터를 N개로 분할
    - 물리적으로 여러개의 작은 BloomFilter로 분할하고, 논리적으로 거대한 단일 BloomFilter
    - App에서 각 Key에 Split 번호를 붙여, N개의 BloomFilter를 거대한 1개의 필터로 취급. 각 Split의 Key가 다르기 때문에, Redis Cluster 샤딩 전략에 의해 각
      Split은 여러 노드에 적절히 분산될 수 있다.

Bloom Filter - Shading

- 블룸필터(split 전략 포함)하여 논리적으로 1개의 필터를 이루므로 단일한 BloomFilter에 모든 부하가 집중 될 수 있음
- 블룸필터를 N개로 sharding 하여 분산
- 각 Key에 샤드번호를 할당하여 여러 Key를 생성. Redis Cluster에서 적절히 분산
- 샤딩전략은 BloomFilter를 물리적/논리적으로 분산 split 전략과 중첩하여 적용 가능