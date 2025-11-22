package com.ctma.allocator;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages a pool of MemoryBlock objects for a single thread.
 * 
 * <p>This pool is designed to be accessed only by a single thread, eliminating
 * the need for synchronization in the common case. When the local pool is
 * exhausted, the allocator will fall back to the shared global pool.
 * 
 * <p>This design reduces lock contention by keeping the hot path (local allocation)
 * completely lock-free.
 * 
 * @author CTMA Project
 */
public class ThreadLocalPool {
    private final Deque<MemoryBlock> freeBlocks;
    private final PaddedAtomicLong localAllocations;
    private final PaddedAtomicLong localFrees;
    private final PaddedAtomicLong hits;
    private final PaddedAtomicLong misses;
    
    /**
     * Creates a new ThreadLocalPool with the specified initial capacity.
     * 
     * @param initialCapacity initial number of blocks to pre-allocate
     * @param blockSize size of each block in bytes
     * @param startId starting ID for blocks (to ensure uniqueness)
     */
    public ThreadLocalPool(int initialCapacity, int blockSize, long startId) {
        this.freeBlocks = new ArrayDeque<>(initialCapacity);
        this.localAllocations = new PaddedAtomicLong();
        this.localFrees = new PaddedAtomicLong();
        this.hits = new PaddedAtomicLong();
        this.misses = new PaddedAtomicLong();
        
        // Pre-populate the pool with initial blocks
        for (int i = 0; i < initialCapacity; i++) {
            MemoryBlock block = new MemoryBlock(startId + i, blockSize);
            block.markFree();
            freeBlocks.offerLast(block);
        }
    }
    
    /**
     * Allocates a block from the local pool.
     * 
     * <p>This operation is lock-free and thread-safe only when called
     * from the owning thread.
     * 
     * @return a MemoryBlock if available, null if the pool is exhausted
     */
    public MemoryBlock allocate() {
        MemoryBlock block = freeBlocks.pollLast();
        if (block != null) {
            block.markUsed();
            localAllocations.incrementAndGet();
            hits.incrementAndGet();
            return block;
        } else {
            misses.incrementAndGet();
            return null;
        }
    }
    
    /**
     * Deallocates a block back to the local pool.
     * 
     * <p>This operation is lock-free and thread-safe only when called
     * from the owning thread.
     * 
     * @param block the block to deallocate
     */
    public void deallocate(MemoryBlock block) {
        if (block != null) {
            block.markFree();
            freeBlocks.offerLast(block);
            localFrees.incrementAndGet();
        }
    }
    
    /**
     * Adds a block to this pool (typically from the global pool).
     * 
     * @param block the block to add
     */
    public void addBlock(MemoryBlock block) {
        if (block != null) {
            block.markFree();
            freeBlocks.offerLast(block);
        }
    }
    
    /**
     * Returns the number of free blocks currently in the pool.
     * 
     * @return number of free blocks
     */
    public int getFreeBlockCount() {
        return freeBlocks.size();
    }
    
    /**
     * Returns the number of local allocations performed.
     * 
     * @return local allocation count
     */
    public long getLocalAllocations() {
        return localAllocations.get();
    }
    
    /**
     * Returns the number of local deallocations performed.
     * 
     * @return local deallocation count
     */
    public long getLocalFrees() {
        return localFrees.get();
    }
    
    /**
     * Returns the number of successful allocations from this pool (hits).
     * 
     * @return hit count
     */
    public long getHits() {
        return hits.get();
    }
    
    /**
     * Returns the number of times the pool was exhausted (misses).
     * 
     * @return miss count
     */
    public long getMisses() {
        return misses.get();
    }
    
    /**
     * Calculates the hit rate as a percentage.
     * 
     * @return hit rate percentage (0.0 to 100.0)
     */
    public double getHitRate() {
        long total = hits.get() + misses.get();
        if (total == 0) {
            return 0.0;
        }
        return (hits.get() * 100.0) / total;
    }
}

