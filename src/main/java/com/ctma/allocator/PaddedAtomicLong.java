package com.ctma.allocator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A utility class that wraps an AtomicLong with padding fields to reduce
 * false sharing in multi-threaded environments.
 * 
 * <p>False sharing occurs when multiple threads update different variables
 * that happen to be on the same cache line (typically 64 bytes on modern CPUs).
 * This causes cache line invalidation and performance degradation.
 * 
 * <p>By adding padding fields before and after the AtomicLong, we increase
 * the likelihood that each PaddedAtomicLong instance occupies its own cache line,
 * preventing false sharing between threads that update different counters.
 * 
 * <p>In a native implementation, this would use compiler directives or
 * explicit memory alignment to ensure cache line separation.
 * 
 * @author CTMA Project
 */
public class PaddedAtomicLong {
    // Padding fields to push the AtomicLong to its own cache line
    // Cache lines are typically 64 bytes, so we add padding to ensure separation
    @SuppressWarnings("unused")
    private long p1, p2, p3, p4, p5, p6, p7; // 56 bytes of padding (7 * 8)
    
    private final AtomicLong value;
    
    @SuppressWarnings("unused")
    private long p8, p9, p10, p11, p12, p13, p14; // 56 bytes of padding after
    
    /**
     * Creates a new PaddedAtomicLong with initial value 0.
     */
    public PaddedAtomicLong() {
        this(0);
    }
    
    /**
     * Creates a new PaddedAtomicLong with the specified initial value.
     * 
     * @param initialValue the initial value
     */
    public PaddedAtomicLong(long initialValue) {
        this.value = new AtomicLong(initialValue);
    }
    
    /**
     * Gets the current value.
     * 
     * @return the current value
     */
    public long get() {
        return value.get();
    }
    
    /**
     * Sets the value to the given new value.
     * 
     * @param newValue the new value
     */
    public void set(long newValue) {
        value.set(newValue);
    }
    
    /**
     * Atomically increments by one and returns the new value.
     * 
     * @return the updated value
     */
    public long incrementAndGet() {
        return value.incrementAndGet();
    }
    
    /**
     * Atomically increments by one and returns the old value.
     * 
     * @return the previous value
     */
    public long getAndIncrement() {
        return value.getAndIncrement();
    }
    
    /**
     * Atomically adds the given value to the current value.
     * 
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGet(long delta) {
        return value.addAndGet(delta);
    }
    
    /**
     * Atomically adds the given value to the current value.
     * 
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAdd(long delta) {
        return value.getAndAdd(delta);
    }
}

