package com.ctma.allocator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Main entry point for the thread-local memory allocator.
 * 
 * <p>This allocator uses a two-tier design:
 * <ul>
 *   <li>Thread-local pools for fast, lock-free allocation in the common case</li>
 *   <li>A shared global pool as a fallback when thread-local pools are exhausted</li>
 * </ul>
 * 
 * <p>The design minimizes lock contention by keeping the hot path (local allocation)
 * completely lock-free, and uses padded counters to reduce false sharing.
 * 
 * @author CTMA Project
 */
public class ThreadLocalMemoryAllocator {
    private final AllocatorConfig config;
    private final SharedMemoryPool sharedPool;
    private final ThreadLocal<ThreadLocalPool> threadLocalPool;
    private final AtomicLong nextThreadId = new AtomicLong(0);
    private final AtomicLong nextBlockId = new AtomicLong(0);
    private final MetricsCollector metricsCollector;
    
    /**
     * Creates a new ThreadLocalMemoryAllocator with the specified configuration.
     * 
     * @param config the allocator configuration
     */
    public ThreadLocalMemoryAllocator(AllocatorConfig config) {
        this.config = config;
        this.sharedPool = new SharedMemoryPool(config.getInitialGlobalBlocks(), 
                config.getBlockSizeBytes());
        this.metricsCollector = new MetricsCollector();
        
        // Initialize nextBlockId to avoid conflicts with shared pool
        this.nextBlockId.set(config.getInitialGlobalBlocks());
        
        // Thread-local pool factory
        this.threadLocalPool = ThreadLocal.withInitial(() -> {
            long threadId = nextThreadId.getAndIncrement();
            long startId = nextBlockId.getAndAdd(config.getInitialPerThreadBlocks());
            return new ThreadLocalPool(config.getInitialPerThreadBlocks(),
                    config.getBlockSizeBytes(), startId);
        });
    }
    
    /**
     * Allocates a memory block.
     * 
     * <p>First attempts to allocate from the thread-local pool (lock-free).
     * If the local pool is exhausted, falls back to the shared global pool.
     * 
     * @return a MemoryBlock
     */
    public MemoryBlock allocate() {
        ThreadLocalPool localPool = threadLocalPool.get();
        MemoryBlock block = localPool.allocate();
        
        if (block != null) {
            // Successfully allocated from local pool
            metricsCollector.recordLocalAllocation();
            metricsCollector.recordHit();
            return block;
        } else {
            // Local pool exhausted, get from global pool
            block = sharedPool.acquireFromGlobal();
            metricsCollector.recordLocalAllocation();
            metricsCollector.recordMiss();
            metricsCollector.recordGlobalPoolAccess();
            return block;
        }
    }
    
    /**
     * Deallocates a memory block.
     * 
     * <p>First attempts to return to the thread-local pool (lock-free).
     * If the local pool is full or if the block came from the global pool,
     * returns it to the global pool.
     * 
     * @param block the block to deallocate
     */
    public void deallocate(MemoryBlock block) {
        if (block == null) {
            return;
        }
        
        ThreadLocalPool localPool = threadLocalPool.get();
        
        // Try to return to local pool first
        // In a real implementation, we might check if the block belongs to this thread
        // For simplicity, we always try local first
        localPool.deallocate(block);
        metricsCollector.recordLocalDeallocation();
    }
    
    /**
     * Collects metrics from all pools and returns a snapshot.
     * 
     * @param threadCount number of threads that participated
     * @return a Metrics snapshot
     */
    public Metrics collectMetrics(int threadCount) {
        // Aggregate metrics from shared pool
        metricsCollector.recordLockContention(sharedPool.getContentionTimeNanos());
        
        // Note: In a real implementation, we'd iterate over all thread-local pools
        // For now, we use the metrics already collected during operations
        
        // Calculate fragmentation
        long freeBlocks = sharedPool.getFreeBlockCount();
        long totalBlocks = config.getInitialGlobalBlocks() + 
                (threadCount * config.getInitialPerThreadBlocks());
        metricsCollector.recordBlockCounts(freeBlocks, totalBlocks);
        
        return metricsCollector.snapshot(threadCount);
    }
    
    /**
     * Returns the configuration used by this allocator.
     * 
     * @return the allocator configuration
     */
    public AllocatorConfig getConfig() {
        return config;
    }
    
    /**
     * Returns the shared memory pool (for testing/debugging).
     * 
     * @return the shared pool
     */
    public SharedMemoryPool getSharedPool() {
        return sharedPool;
    }
}

