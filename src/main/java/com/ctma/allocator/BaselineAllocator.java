package com.ctma.allocator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Baseline allocator that simulates standard malloc-like behavior.
 * 
 * <p>This allocator uses a single global pool protected by a ReentrantLock,
 * representing the traditional approach with high lock contention.
 * 
 * <p>This is used for comparison against the thread-local allocator to
 * demonstrate the performance benefits of the CTMA approach.
 * 
 * @author CTMA Project
 */
public class BaselineAllocator {
    private final Deque<MemoryBlock> freeBlocks;
    private final ReentrantLock lock;
    private final int blockSize;
    private final AtomicLong nextBlockId;
    
    // Contention tracking
    private long contentionTimeNanos = 0;
    private long contentionEvents = 0;
    private long totalAllocations = 0;
    private long totalDeallocations = 0;
    
    /**
     * Creates a new BaselineAllocator with the specified initial capacity.
     * 
     * @param initialCapacity initial number of blocks to pre-allocate
     * @param blockSize size of each block in bytes
     */
    public BaselineAllocator(int initialCapacity, int blockSize) {
        this.freeBlocks = new ArrayDeque<>(initialCapacity);
        this.lock = new ReentrantLock();
        this.blockSize = blockSize;
        this.nextBlockId = new AtomicLong(0);
        
        // Pre-populate the pool
        for (int i = 0; i < initialCapacity; i++) {
            MemoryBlock block = new MemoryBlock(nextBlockId.getAndIncrement(), blockSize);
            block.markFree();
            freeBlocks.offerLast(block);
        }
    }
    
    /**
     * Allocates a block from the pool.
     * 
     * <p>This operation requires acquiring the lock, which can cause
     * contention in multi-threaded scenarios.
     * 
     * @return a MemoryBlock
     */
    public MemoryBlock allocate() {
        long startTime = System.nanoTime();
        lock.lock();
        try {
            long lockTime = System.nanoTime() - startTime;
            if (lockTime > 0) {
                contentionTimeNanos += lockTime;
                contentionEvents++;
            }
            
            totalAllocations++;
            MemoryBlock block = freeBlocks.pollLast();
            
            // If pool is empty, allocate a new block
            if (block == null) {
                block = new MemoryBlock(nextBlockId.getAndIncrement(), blockSize);
            }
            
            block.markUsed();
            return block;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Deallocates a block back to the pool.
     * 
     * <p>This operation requires acquiring the lock, which can cause
     * contention in multi-threaded scenarios.
     * 
     * @param block the block to deallocate
     */
    public void deallocate(MemoryBlock block) {
        if (block == null) {
            return;
        }
        
        long startTime = System.nanoTime();
        lock.lock();
        try {
            long lockTime = System.nanoTime() - startTime;
            if (lockTime > 0) {
                contentionTimeNanos += lockTime;
                contentionEvents++;
            }
            
            totalDeallocations++;
            block.markFree();
            freeBlocks.offerLast(block);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns the total lock contention time in nanoseconds.
     * 
     * @return contention time in nanoseconds
     */
    public long getContentionTimeNanos() {
        return contentionTimeNanos;
    }
    
    /**
     * Returns the number of contention events.
     * 
     * @return contention event count
     */
    public long getContentionEvents() {
        return contentionEvents;
    }
    
    /**
     * Returns the total number of allocations.
     * 
     * @return allocation count
     */
    public long getTotalAllocations() {
        return totalAllocations;
    }
    
    /**
     * Returns the total number of deallocations.
     * 
     * @return deallocation count
     */
    public long getTotalDeallocations() {
        return totalDeallocations;
    }
    
    /**
     * Returns the number of free blocks currently in the pool.
     * 
     * @return number of free blocks
     */
    public int getFreeBlockCount() {
        lock.lock();
        try {
            return freeBlocks.size();
        } finally {
            lock.unlock();
        }
    }
}

