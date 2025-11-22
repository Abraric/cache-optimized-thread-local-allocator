package com.ctma.allocator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe collector for aggregating metrics from all pools and the allocator.
 * 
 * <p>This class collects metrics from multiple threads and pools, providing
 * a unified view of allocator performance.
 * 
 * @author CTMA Project
 */
public class MetricsCollector {
    private final AtomicLong totalAllocations = new AtomicLong();
    private final AtomicLong totalDeallocations = new AtomicLong();
    private final AtomicLong totalHits = new AtomicLong();
    private final AtomicLong totalMisses = new AtomicLong();
    private final AtomicLong globalPoolAccesses = new AtomicLong();
    private final AtomicLong totalContentionTimeNanos = new AtomicLong();
    private final AtomicLong totalContentionEvents = new AtomicLong();
    private final AtomicLong totalFreeBlocks = new AtomicLong();
    private final AtomicLong totalAllocatedBlocks = new AtomicLong();
    
    /**
     * Records a local allocation (from thread-local pool).
     */
    public void recordLocalAllocation() {
        totalAllocations.incrementAndGet();
    }
    
    /**
     * Records a local deallocation (to thread-local pool).
     */
    public void recordLocalDeallocation() {
        totalDeallocations.incrementAndGet();
    }
    
    /**
     * Records a hit (successful allocation from local pool).
     */
    public void recordHit() {
        totalHits.incrementAndGet();
    }
    
    /**
     * Records a miss (local pool exhausted, had to use global pool).
     */
    public void recordMiss() {
        totalMisses.incrementAndGet();
    }
    
    /**
     * Records an access to the global pool.
     */
    public void recordGlobalPoolAccess() {
        globalPoolAccesses.incrementAndGet();
    }
    
    /**
     * Records lock contention time.
     * 
     * @param nanos contention time in nanoseconds
     */
    public void recordLockContention(long nanos) {
        totalContentionTimeNanos.addAndGet(nanos);
        totalContentionEvents.incrementAndGet();
    }
    
    /**
     * Records block counts for fragmentation calculation.
     * 
     * @param freeBlocks number of free blocks
     * @param totalBlocks total number of blocks
     */
    public void recordBlockCounts(long freeBlocks, long totalBlocks) {
        totalFreeBlocks.set(freeBlocks);
        totalAllocatedBlocks.set(totalBlocks);
    }
    
    /**
     * Takes a snapshot of current metrics.
     * 
     * @param threadCount number of threads that participated
     * @return a Metrics snapshot
     */
    public Metrics snapshot(int threadCount) {
        long allocations = totalAllocations.get();
        long deallocations = totalDeallocations.get();
        long hits = totalHits.get();
        long misses = totalMisses.get();
        long globalAccesses = globalPoolAccesses.get();
        long contentionNanos = totalContentionTimeNanos.get();
        long contentionEvents = totalContentionEvents.get();
        
        // Calculate hit rate
        double hitRate = 0.0;
        long totalAttempts = hits + misses;
        if (totalAttempts > 0) {
            hitRate = (hits * 100.0) / totalAttempts;
        }
        
        // Calculate fragmentation (simplified: free blocks / total blocks)
        double fragmentation = 0.0;
        long totalBlocks = totalAllocatedBlocks.get();
        if (totalBlocks > 0) {
            long freeBlocks = totalFreeBlocks.get();
            fragmentation = (freeBlocks * 100.0) / totalBlocks;
        }
        
        return new Metrics(allocations, deallocations, hitRate, globalAccesses,
                fragmentation, contentionNanos, contentionEvents, threadCount);
    }
    
    /**
     * Resets all collected metrics.
     */
    public void reset() {
        totalAllocations.set(0);
        totalDeallocations.set(0);
        totalHits.set(0);
        totalMisses.set(0);
        globalPoolAccesses.set(0);
        totalContentionTimeNanos.set(0);
        totalContentionEvents.set(0);
        totalFreeBlocks.set(0);
        totalAllocatedBlocks.set(0);
    }
}

