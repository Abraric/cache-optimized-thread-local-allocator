package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaselineAllocator.
 */
class BaselineAllocatorTest {
    
    @Test
    void testAllocate() {
        BaselineAllocator allocator = new BaselineAllocator(10, 1024);
        MemoryBlock block = allocator.allocate();
        
        assertNotNull(block);
        assertTrue(block.isInUse());
        assertEquals(1024, block.getSize());
        assertEquals(9, allocator.getFreeBlockCount());
    }
    
    @Test
    void testDeallocate() {
        BaselineAllocator allocator = new BaselineAllocator(10, 1024);
        MemoryBlock block = allocator.allocate();
        allocator.deallocate(block);
        
        assertFalse(block.isInUse());
        assertEquals(10, allocator.getFreeBlockCount());
    }
    
    @Test
    void testReuse() {
        BaselineAllocator allocator = new BaselineAllocator(10, 1024);
        
        MemoryBlock block1 = allocator.allocate();
        long id1 = block1.getId();
        allocator.deallocate(block1);
        
        MemoryBlock block2 = allocator.allocate();
        assertEquals(id1, block2.getId()); // Should reuse
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        BaselineAllocator allocator = new BaselineAllocator(100, 1024);
        int threadCount = 10;
        int operationsPerThread = 20;
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    MemoryBlock block = allocator.allocate();
                    assertNotNull(block);
                    allocator.deallocate(block);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // All blocks should be returned
        assertTrue(allocator.getFreeBlockCount() >= 100);
    }
    
    @Test
    void testNewBlockAllocation() {
        BaselineAllocator allocator = new BaselineAllocator(0, 512);
        
        // Pool is empty, should allocate new block
        MemoryBlock block = allocator.allocate();
        assertNotNull(block);
        assertEquals(512, block.getSize());
    }
    
    @Test
    void testMetrics() {
        BaselineAllocator allocator = new BaselineAllocator(10, 1024);
        
        allocator.allocate();
        allocator.allocate();
        
        assertEquals(2, allocator.getTotalAllocations());
        
        MemoryBlock block = allocator.allocate();
        allocator.deallocate(block);
        
        assertEquals(3, allocator.getTotalAllocations());
        assertEquals(1, allocator.getTotalDeallocations());
    }
}

