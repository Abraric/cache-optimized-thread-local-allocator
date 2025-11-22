# Problem Statement

## Domain

**Operating Systems, Concurrent Programming, System Performance**

## Problem

In modern multi-core systems, memory allocation is a critical performance bottleneck. Traditional allocators face several challenges:

### 1. Lock Contention

Standard memory allocators (like `malloc` in C or similar patterns in Java) typically use a single global pool protected by locks. When multiple threads compete for memory allocation, they must acquire and release locks, leading to:

- **Serialization**: Only one thread can allocate at a time
- **Cache line bouncing**: Lock variables are frequently invalidated across CPU cores
- **Context switching**: Threads block waiting for locks, causing expensive context switches
- **Scalability degradation**: Performance decreases as the number of threads increases

### 2. False Sharing

False sharing occurs when multiple threads update different variables that happen to reside on the same CPU cache line (typically 64 bytes). Even though the threads are accessing different memory locations, the cache coherence protocol treats the entire cache line as modified, causing:

- **Cache line invalidation**: Other CPUs must reload the cache line
- **Memory bandwidth waste**: Unnecessary data transfer between CPU caches
- **Performance degradation**: Can reduce performance by 10-100x in worst cases

### 3. Cache Unfriendliness

Traditional allocators often have poor cache locality:

- **Random memory access patterns**: Blocks are allocated from a global pool, leading to cache misses
- **Fragmentation**: Memory becomes fragmented, reducing cache efficiency
- **Poor spatial locality**: Related allocations are not grouped together

## Real-World Relevance

These problems manifest in various high-performance systems:

### Task Schedulers

- **Problem**: Thousands of tasks allocate/deallocate memory concurrently
- **Impact**: Lock contention becomes the bottleneck, limiting throughput
- **Example**: Work-stealing schedulers in parallel frameworks (Fork/Join, TBB)

### Game Engines

- **Problem**: Per-frame allocation of game objects, particles, and temporary buffers
- **Impact**: Frame rate drops due to allocation overhead
- **Example**: Real-time rendering systems requiring consistent 60+ FPS

### Network Servers

- **Problem**: Each request may allocate buffers, causing high contention
- **Impact**: Throughput degrades under high load
- **Example**: Web servers, database systems, message queues

### High-Frequency Trading

- **Problem**: Microsecond-level latency requirements
- **Impact**: Any lock contention adds unacceptable latency
- **Example**: Order matching engines, market data processors

## Solution Approach

The **Cache-Optimized Thread-Local Memory Allocator (CTMA)** addresses these issues by:

1. **Thread-Local Pools**: Each thread has its own memory pool, eliminating lock contention for the common case
2. **Cache-Line Padding**: Critical counters are padded to avoid false sharing
3. **Lock-Free Local Operations**: Local allocations/deallocations require no synchronization
4. **Efficient Global Fallback**: When local pools are exhausted, a lock-free shared pool provides blocks

This design ensures that the hot path (local allocation) is completely lock-free and cache-friendly, while maintaining correctness and scalability.

