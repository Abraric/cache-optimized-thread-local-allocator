package com.ctma.benchmarks;

import com.ctma.allocator.AllocatorConfig;
import com.ctma.allocator.MemoryBlock;
import com.ctma.allocator.ThreadLocalMemoryAllocator;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for the CTMA Thread-Local Memory Allocator.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class CTMAThreadLocalAllocatorBenchmark {
    
    @Param({"1", "2", "4", "8", "16"})
    public int threadCount;
    
    @Param({"1024"})
    public int blockSize;
    
    @Param({"100000"})
    public int operationsPerThread;
    
    private ThreadLocalMemoryAllocator allocator;
    
    @Setup(Level.Trial)
    public void setup() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(blockSize)
                .initialGlobalBlocks(threadCount * 100)
                .initialPerThreadBlocks(100)
                .build();
        
        allocator = new ThreadLocalMemoryAllocator(config);
    }
    
    @Benchmark
    @Threads(1)
    public void benchmarkSingleThread() {
        if (threadCount == 1) {
            runWorkload();
        }
    }
    
    @Benchmark
    @Threads(2)
    public void benchmarkTwoThreads() {
        if (threadCount == 2) {
            runWorkload();
        }
    }
    
    @Benchmark
    @Threads(4)
    public void benchmarkFourThreads() {
        if (threadCount == 4) {
            runWorkload();
        }
    }
    
    @Benchmark
    @Threads(8)
    public void benchmarkEightThreads() {
        if (threadCount == 8) {
            runWorkload();
        }
    }
    
    @Benchmark
    @Threads(16)
    public void benchmarkSixteenThreads() {
        if (threadCount == 16) {
            runWorkload();
        }
    }
    
    private void runWorkload() {
        List<MemoryBlock> blocks = new ArrayList<>();
        
        // Allocate
        for (int i = 0; i < operationsPerThread; i++) {
            MemoryBlock block = allocator.allocate();
            blocks.add(block);
            
            // Deallocate every 10th block to create churn
            if (i % 10 == 0 && !blocks.isEmpty()) {
                MemoryBlock toFree = blocks.remove(blocks.size() - 1);
                allocator.deallocate(toFree);
            }
        }
        
        // Deallocate remaining blocks
        for (MemoryBlock block : blocks) {
            allocator.deallocate(block);
        }
    }
}

