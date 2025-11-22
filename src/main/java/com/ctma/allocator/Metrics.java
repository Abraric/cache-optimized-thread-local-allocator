package com.ctma.allocator;

/**
 * Aggregates metrics collected from pools and the allocator.
 * 
 * <p>This class provides a snapshot of allocator performance metrics,
 * including throughput, hit rates, contention, and fragmentation.
 * 
 * @author CTMA Project
 */
public class Metrics {
    private final long totalAllocations;
    private final long totalDeallocations;
    private final double localHitRate;
    private final long globalPoolAccesses;
    private final double fragmentation;
    private final long lockContentionTimeNanos;
    private final long lockContentionEvents;
    private final int threadCount;
    
    /**
     * Creates a new Metrics instance with the specified values.
     * 
     * @param totalAllocations total number of allocations
     * @param totalDeallocations total number of deallocations
     * @param localHitRate local pool hit rate as a percentage (0.0-100.0)
     * @param globalPoolAccesses number of accesses to the global pool
     * @param fragmentation fragmentation percentage (0.0-100.0)
     * @param lockContentionTimeNanos total lock contention time in nanoseconds
     * @param lockContentionEvents number of lock contention events
     * @param threadCount number of threads that participated
     */
    public Metrics(long totalAllocations, long totalDeallocations, double localHitRate,
                   long globalPoolAccesses, double fragmentation, long lockContentionTimeNanos,
                   long lockContentionEvents, int threadCount) {
        this.totalAllocations = totalAllocations;
        this.totalDeallocations = totalDeallocations;
        this.localHitRate = localHitRate;
        this.globalPoolAccesses = globalPoolAccesses;
        this.fragmentation = fragmentation;
        this.lockContentionTimeNanos = lockContentionTimeNanos;
        this.lockContentionEvents = lockContentionEvents;
        this.threadCount = threadCount;
    }
    
    /**
     * Returns the total number of allocations.
     * 
     * @return total allocations
     */
    public long getTotalAllocations() {
        return totalAllocations;
    }
    
    /**
     * Returns the total number of deallocations.
     * 
     * @return total deallocations
     */
    public long getTotalDeallocations() {
        return totalDeallocations;
    }
    
    /**
     * Returns the local pool hit rate as a percentage.
     * 
     * @return hit rate (0.0-100.0)
     */
    public double getLocalHitRate() {
        return localHitRate;
    }
    
    /**
     * Returns the number of accesses to the global pool.
     * 
     * @return global pool access count
     */
    public long getGlobalPoolAccesses() {
        return globalPoolAccesses;
    }
    
    /**
     * Returns the fragmentation percentage.
     * 
     * @return fragmentation (0.0-100.0)
     */
    public double getFragmentation() {
        return fragmentation;
    }
    
    /**
     * Returns the total lock contention time in nanoseconds.
     * 
     * @return contention time in nanoseconds
     */
    public long getLockContentionTimeNanos() {
        return lockContentionTimeNanos;
    }
    
    /**
     * Returns the total lock contention time in milliseconds.
     * 
     * @return contention time in milliseconds
     */
    public double getLockContentionTimeMs() {
        return lockContentionTimeNanos / 1_000_000.0;
    }
    
    /**
     * Returns the number of lock contention events.
     * 
     * @return contention event count
     */
    public long getLockContentionEvents() {
        return lockContentionEvents;
    }
    
    /**
     * Returns the number of threads that participated.
     * 
     * @return thread count
     */
    public int getThreadCount() {
        return threadCount;
    }
    
    /**
     * Returns a formatted string representation of the metrics.
     * 
     * @return formatted metrics string
     */
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append("CTMA Metrics\n");
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Threads: %d\n", threadCount));
        sb.append(String.format("Total Allocations: %d\n", totalAllocations));
        sb.append(String.format("Total Deallocations: %d\n", totalDeallocations));
        sb.append(String.format("Local Pool Hit Rate: %.2f%%\n", localHitRate));
        sb.append(String.format("Global Pool Accesses: %d\n", globalPoolAccesses));
        sb.append(String.format("Fragmentation: %.2f%%\n", fragmentation));
        sb.append(String.format("Lock Contention Time: %.2f ms\n", getLockContentionTimeMs()));
        sb.append(String.format("Lock Events: %d\n", lockContentionEvents));
        sb.append("--------------------------------------------------\n");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Metrics[allocations=%d, hitRate=%.2f%%, contention=%.2fms]",
                totalAllocations, localHitRate, getLockContentionTimeMs());
    }
}

