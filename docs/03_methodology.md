# Methodology

## Design Overview

The CTMA allocator uses a **two-tier architecture** to balance performance and resource utilization:

```
┌─────────────────────────────────────────┐
│     Thread 1                            │
│  ┌──────────────────────┐               │
│  │ ThreadLocalPool      │               │
│  │ (Lock-Free)         │               │
│  └──────────────────────┘               │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│     Thread 2                            │
│  ┌──────────────────────┐               │
│  │ ThreadLocalPool      │               │
│  │ (Lock-Free)         │               │
│  └──────────────────────┘               │
└─────────────────────────────────────────┘

         │              │
         └──────┬───────┘
                │
                ▼
    ┌───────────────────────┐
    │  SharedMemoryPool     │
    │  (Lock-Free Queue)    │
    └───────────────────────┘
```

## Thread-Local Design

### ThreadLocalPool

Each thread maintains its own pool of `MemoryBlock` objects:

- **Data Structure**: `ArrayDeque<MemoryBlock>` for O(1) operations
- **Operations**: 
  - `allocate()`: Remove from tail (lock-free, thread-local)
  - `deallocate()`: Add to tail (lock-free, thread-local)
- **Metrics**: Tracks hits, misses, allocations, and deallocations using `PaddedAtomicLong`

### Benefits

1. **No Lock Contention**: Each thread operates on its own data structure
2. **Cache Locality**: Data stays in the same CPU cache
3. **Predictable Performance**: No interference from other threads

## Shared Pool Design

### SharedMemoryPool

When local pools are exhausted, threads acquire blocks from a shared pool:

- **Data Structure**: `ConcurrentLinkedQueue<MemoryBlock>` (lock-free)
- **Operations**:
  - `acquireFromGlobal()`: Poll from queue (lock-free CAS operations)
  - `releaseToGlobal()`: Offer to queue (lock-free CAS operations)
- **Contention Tracking**: Measures time spent in CAS operations

### Benefits

1. **Minimal Contention**: Lock-free queue reduces synchronization overhead
2. **Scalability**: Multiple threads can access concurrently
3. **Fallback Safety**: Ensures blocks are always available

## False Sharing Mitigation

### PaddedAtomicLong

Critical counters are wrapped in a padded structure:

```java
public class PaddedAtomicLong {
    private long p1, p2, p3, p4, p5, p6, p7;  // 56 bytes padding
    private final AtomicLong value;            // 8 bytes
    private long p8, p9, p10, p11, p12, p13, p14; // 56 bytes padding
}
```

**Total Size**: ~120 bytes (ensures separate cache line on 64-byte cache lines)

### Spatial Separation

- Each `ThreadLocalPool` instance is isolated per thread
- Counters are separated by padding
- No two threads update the same cache line in steady state

## Benchmark Setup

### Workload

Each benchmark thread performs:

1. **Allocation Phase**: Allocate `N` blocks
2. **Churn**: Deallocate every 10th block to simulate real-world patterns
3. **Deallocation Phase**: Deallocate all remaining blocks

### Parameters

- **Thread Counts**: 1, 2, 4, 8, 16
- **Operations per Thread**: 100,000 - 1,000,000
- **Block Size**: 1KB (configurable)

### Metrics Collected

1. **Throughput**: Operations per second
2. **Hit Rate**: Percentage of allocations from local pool
3. **Contention Time**: Time spent waiting for locks/CAS
4. **Contention Events**: Number of contended operations
5. **Fragmentation**: Percentage of free blocks

## Metrics Definitions

### Local Pool Hit Rate

```
Hit Rate = (Local Allocations / Total Allocation Attempts) × 100%
```

Measures how often the local pool satisfies requests without falling back to the global pool.

### Lock Contention Time

Total time spent waiting for locks or in contended CAS operations, measured in nanoseconds.

### Fragmentation

```
Fragmentation = (Free Blocks / Total Blocks) × 100%
```

Simplified metric indicating how much memory is available but not in use.

### Throughput

```
Throughput = (Total Operations / Duration) ops/sec
```

Measures the overall performance of the allocator.

## Comparison Methodology

### Baseline Allocator

The baseline allocator uses:

- Single global pool (`ArrayDeque`)
- `ReentrantLock` for synchronization
- Represents traditional malloc-like behavior

### Comparison Metrics

1. **Throughput**: ops/sec for both allocators
2. **Contention Reduction**: Percentage reduction in lock contention
3. **Scalability**: How performance changes with thread count
4. **Hit Rate**: CTMA-specific metric (baseline has 0% local hit rate)

## Implementation Notes

### Java-Specific Considerations

Since this is a Java implementation (not native code):

- We simulate memory allocation using `byte[]` arrays
- Cache line padding is probabilistic (JVM may rearrange fields)
- False sharing mitigation is conceptual but demonstrates the principle
- In a native implementation, explicit cache line alignment would be used

### Limitations

1. **JVM Memory Management**: Java's GC still manages underlying memory
2. **Field Reordering**: JVM may reorder fields, affecting padding effectiveness
3. **Simulation**: This models allocator behavior, not replacing JVM allocation

