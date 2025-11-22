package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThreadLocalMemoryAllocator.
 */
class ThreadLocalMemoryAllocatorTest {
    
    @Test
    void testAllocate() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(100)
                .initialPerThreadBlocks(10)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        MemoryBlock block = allocator.allocate();
        
        assertNotNull(block);
        assertTrue(block.isInUse());
        assertEquals(1024, block.getSize());
    }
    
    @Test
    void testDeallocate() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(100)
                .initialPerThreadBlocks(10)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        MemoryBlock block = allocator.allocate();
        allocator.deallocate(block);
        
        assertFalse(block.isInUse());
    }
    
    @Test
    void testReuse() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(100)
                .initialPerThreadBlocks(10)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        
        MemoryBlock block1 = allocator.allocate();
        long id1 = block1.getId();
        allocator.deallocate(block1);
        
        MemoryBlock block2 = allocator.allocate();
        // Should reuse from local pool
        assertEquals(id1, block2.getId());
    }
    
    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(100)
                .initialPerThreadBlocks(5)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        
        // Allocate from main thread
        MemoryBlock mainBlock = allocator.allocate();
        
        // Allocate from another thread
        MemoryBlock[] otherBlock = new MemoryBlock[1];
        Thread thread = new Thread(() -> {
            otherBlock[0] = allocator.allocate();
        });
        thread.start();
        thread.join();
        
        assertNotNull(mainBlock);
        assertNotNull(otherBlock[0]);
        // Each thread should have its own pool
        assertNotEquals(mainBlock.getId(), otherBlock[0].getId());
    }
    
    @Test
    void testGlobalPoolFallback() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(10)
                .initialPerThreadBlocks(2) // Small local pool
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        
        // Exhaust local pool
        MemoryBlock block1 = allocator.allocate();
        MemoryBlock block2 = allocator.allocate();
        
        // Next allocation should come from global pool
        MemoryBlock block3 = allocator.allocate();
        assertNotNull(block3);
    }
    
    @Test
    void testMetricsCollection() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(1024)
                .initialGlobalBlocks(100)
                .initialPerThreadBlocks(10)
                .build();
        
        ThreadLocalMemoryAllocator allocator = new ThreadLocalMemoryAllocator(config);
        
        for (int i = 0; i < 20; i++) {
            MemoryBlock block = allocator.allocate();
            allocator.deallocate(block);
        }
        
        Metrics metrics = allocator.collectMetrics(1);
        assertNotNull(metrics);
        assertTrue(metrics.getTotalAllocations() > 0);
    }
}

