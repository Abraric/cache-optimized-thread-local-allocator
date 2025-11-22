package com.ctma.allocator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demo application that demonstrates the CTMA allocator performance.
 * 
 * <p>This demo spawns multiple worker threads that perform allocate/deallocate
 * cycles, then prints comprehensive metrics comparing the CTMA allocator
 * against a baseline allocator.
 * 
 * @author CTMA Project
 */
public class AllocatorDemo {
    private static final int DEFAULT_THREAD_COUNT = 16;
    private static final int DEFAULT_OPERATIONS_PER_THREAD = 100_000;
    private static final int DEFAULT_BLOCK_SIZE = 1024; // 1KB
    
    /**
     * Main entry point for the demo.
     * 
     * @param args command-line arguments (optional: threadCount, operationsPerThread)
     */
    public static void main(String[] args) {
        int threadCount = DEFAULT_THREAD_COUNT;
        int operationsPerThread = DEFAULT_OPERATIONS_PER_THREAD;
        
        if (args.length >= 1) {
            threadCount = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            operationsPerThread = Integer.parseInt(args[1]);
        }
        
        System.out.println("==========================================");
        System.out.println("CTMA Allocator Demo");
        System.out.println("==========================================");
        System.out.println("Threads: " + threadCount);
        System.out.println("Operations per thread: " + operationsPerThread);
        System.out.println("Total operations: " + (threadCount * operationsPerThread));
        System.out.println();
        
        // Run CTMA allocator benchmark
        System.out.println("Running CTMA Allocator benchmark...");
        Metrics ctmaMetrics = runCTMABenchmark(threadCount, operationsPerThread);
        System.out.println(ctmaMetrics.toPrettyString());
        
        System.out.println();
        
        // Run baseline allocator benchmark
        System.out.println("Running Baseline Allocator benchmark...");
        Metrics baselineMetrics = runBaselineBenchmark(threadCount, operationsPerThread);
        System.out.println(baselineMetrics.toPrettyString());
        
        System.out.println();
        System.out.println("==========================================");
        System.out.println("Comparison Summary");
        System.out.println("==========================================");
        printComparison(ctmaMetrics, baselineMetrics);
    }
    
    /**
     * Runs the CTMA allocator benchmark.
     * 
     * @param threadCount number of worker threads
     * @param operationsPerThread operations per thread
     * @return collected metrics
     */
    private static Metrics runCTMABenchmark(int threadCount, int operationsPerThread) {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(DEFAULT_BLOCK_SIZE)
                .initialGlobalBlocks(threadCount * 100)
                .initialPerThreadBlocks(100)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    List<MemoryBlock> blocks = new ArrayList<>();
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Allocate
                        MemoryBlock block = allocator.allocate();
                        blocks.add(block);
                        
                        // Simulate some work
                        if (j % 10 == 0) {
                            // Deallocate every 10th block to create some churn
                            if (!blocks.isEmpty()) {
                                MemoryBlock toFree = blocks.remove(blocks.size() - 1);
                                allocator.deallocate(toFree);
                            }
                        }
                    }
                    
                    // Deallocate remaining blocks
                    for (MemoryBlock block : blocks) {
                        allocator.deallocate(block);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            
            Metrics metrics = allocator.collectMetrics(threadCount);
            
            // Calculate throughput
            long totalOps = threadCount * operationsPerThread * 2; // allocate + deallocate
            double throughput = (totalOps * 1_000_000_000.0) / durationNanos;
            
            System.out.println(String.format("Duration: %.2f ms", durationNanos / 1_000_000.0));
            System.out.println(String.format("Throughput: %.2f ops/sec", throughput));
            
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return metrics;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdown();
            return null;
        }
    }
    
    /**
     * Runs the baseline allocator benchmark.
     * 
     * @param threadCount number of worker threads
     * @param operationsPerThread operations per thread
     * @return collected metrics
     */
    private static Metrics runBaselineBenchmark(int threadCount, int operationsPerThread) {
        BaselineAllocator allocator = new BaselineAllocator(
                threadCount * 100, DEFAULT_BLOCK_SIZE);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    List<MemoryBlock> blocks = new ArrayList<>();
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Allocate
                        MemoryBlock block = allocator.allocate();
                        blocks.add(block);
                        
                        // Simulate some work
                        if (j % 10 == 0) {
                            // Deallocate every 10th block
                            if (!blocks.isEmpty()) {
                                MemoryBlock toFree = blocks.remove(blocks.size() - 1);
                                allocator.deallocate(toFree);
                            }
                        }
                    }
                    
                    // Deallocate remaining blocks
                    for (MemoryBlock block : blocks) {
                        allocator.deallocate(block);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            
            // Create metrics from baseline allocator
            long totalAllocations = allocator.getTotalAllocations();
            long totalDeallocations = allocator.getTotalDeallocations();
            long contentionNanos = allocator.getContentionTimeNanos();
            long contentionEvents = allocator.getContentionEvents();
            
            Metrics metrics = new Metrics(totalAllocations, totalDeallocations, 0.0,
                    0, 0.0, contentionNanos, contentionEvents, threadCount);
            
            // Calculate throughput
            long totalOps = threadCount * operationsPerThread * 2;
            double throughput = (totalOps * 1_000_000_000.0) / durationNanos;
            
            System.out.println(String.format("Duration: %.2f ms", durationNanos / 1_000_000.0));
            System.out.println(String.format("Throughput: %.2f ops/sec", throughput));
            
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return metrics;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdown();
            return null;
        }
    }
    
    /**
     * Prints a comparison between CTMA and baseline metrics.
     * 
     * @param ctma CTMA metrics
     * @param baseline baseline metrics
     */
    private static void printComparison(Metrics ctma, Metrics baseline) {
        if (ctma == null || baseline == null) {
            return;
        }
        
        double ctmaContention = ctma.getLockContentionTimeMs();
        double baselineContention = baseline.getLockContentionTimeMs();
        double contentionReduction = baselineContention > 0 
                ? ((baselineContention - ctmaContention) / baselineContention) * 100.0
                : 0.0;
        
        System.out.println(String.format("Lock Contention Reduction: %.2f%%", 
                Math.max(0, contentionReduction)));
        System.out.println(String.format("CTMA Hit Rate: %.2f%%", ctma.getLocalHitRate()));
        System.out.println(String.format("CTMA Contention Events: %d", 
                ctma.getLockContentionEvents()));
        System.out.println(String.format("Baseline Contention Events: %d", 
                baseline.getLockContentionEvents()));
    }
}

