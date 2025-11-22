package com.ctma.allocator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central shared pool used when a ThreadLocalPool is exhausted.
 * 
 * <p>This pool is thread-safe and uses lock-free data structures where possible.
 * It tracks lock contention metrics to measure the cost of synchronization
 * in multi-threaded scenarios.
 * 
 * <p>In a native implementation, this would use more sophisticated lock-free
 * algorithms or fine-grained locking to minimize contention.
 * 
 * @author CTMA Project
 */
public class SharedMemoryPool {
    private final ConcurrentLinkedQueue<MemoryBlock> freeBlocks;
    private final AtomicLong nextBlockId;
    private final int blockSize;
    
    // Contention tracking
    private final PaddedAtomicLong contentionTimeNanos;
    private final PaddedAtomicLong contentionEvents;
    private final PaddedAtomicLong globalAcquires;
    private final PaddedAtomicLong globalReleases;
    
    /**
     * Creates a new SharedMemoryPool with the specified initial capacity.
     * 
     * @param initialCapacity initial number of blocks to pre-allocate
     * @param blockSize size of each block in bytes
     */
    public SharedMemoryPool(int initialCapacity, int blockSize) {
        this.freeBlocks = new ConcurrentLinkedQueue<>();
        this.nextBlockId = new AtomicLong(0);
        this.blockSize = blockSize;
        this.contentionTimeNanos = new PaddedAtomicLong();
        this.contentionEvents = new PaddedAtomicLong();
        this.globalAcquires = new PaddedAtomicLong();
        this.globalReleases = new PaddedAtomicLong();
        
        // Pre-populate the pool
        for (int i = 0; i < initialCapacity; i++) {
            MemoryBlock block = new MemoryBlock(nextBlockId.getAndIncrement(), blockSize);
            block.markFree();
            freeBlocks.offer(block);
        }
    }
    
    /**
     * Acquires a block from the global pool.
     * 
     * <p>This operation is lock-free (using ConcurrentLinkedQueue), but we
     * track contention by measuring time spent in CAS operations.
     * 
     * @return a MemoryBlock if available, or a newly allocated one if pool is empty
     */
    public MemoryBlock acquireFromGlobal() {
        long startTime = System.nanoTime();
        globalAcquires.incrementAndGet();
        
        MemoryBlock block = freeBlocks.poll();
        
        // If pool is empty, allocate a new block
        if (block == null) {
            block = new MemoryBlock(nextBlockId.getAndIncrement(), blockSize);
        }
        
        block.markUsed();
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Track contention if operation took longer than expected
        // (This is a heuristic - in a real system, we'd track actual CAS failures)
        if (duration > 1000) { // More than 1 microsecond suggests contention
            contentionTimeNanos.addAndGet(duration);
            contentionEvents.incrementAndGet();
        }
        
        return block;
    }
    
    /**
     * Releases a block back to the global pool.
     * 
     * @param block the block to release
     */
    public void releaseToGlobal(MemoryBlock block) {
        if (block != null) {
            long startTime = System.nanoTime();
            globalReleases.incrementAndGet();
            
            block.markFree();
            freeBlocks.offer(block);
            
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            
            // Track contention
            if (duration > 1000) {
                contentionTimeNanos.addAndGet(duration);
                contentionEvents.incrementAndGet();
            }
        }
    }
    
    /**
     * Returns the number of free blocks currently in the pool.
     * 
     * @return number of free blocks (approximate, as this is a concurrent structure)
     */
    public int getFreeBlockCount() {
        return freeBlocks.size();
    }
    
    /**
     * Returns the total time spent in contended operations (nanoseconds).
     * 
     * @return total contention time in nanoseconds
     */
    public long getContentionTimeNanos() {
        return contentionTimeNanos.get();
    }
    
    /**
     * Returns the number of contention events detected.
     * 
     * @return contention event count
     */
    public long getContentionEvents() {
        return contentionEvents.get();
    }
    
    /**
     * Returns the number of blocks acquired from this pool.
     * 
     * @return global acquire count
     */
    public long getGlobalAcquires() {
        return globalAcquires.get();
    }
    
    /**
     * Returns the number of blocks released to this pool.
     * 
     * @return global release count
     */
    public long getGlobalReleases() {
        return globalReleases.get();
    }
}

