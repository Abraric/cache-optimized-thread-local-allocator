package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SharedMemoryPool.
 */
class SharedMemoryPoolTest {
    
    @Test
    void testAcquireFromGlobal() {
        SharedMemoryPool pool = new SharedMemoryPool(10, 1024);
        MemoryBlock block = pool.acquireFromGlobal();
        
        assertNotNull(block);
        assertTrue(block.isInUse());
        assertEquals(9, pool.getFreeBlockCount());
    }
    
    @Test
    void testReleaseToGlobal() {
        SharedMemoryPool pool = new SharedMemoryPool(10, 1024);
        MemoryBlock block = pool.acquireFromGlobal();
        pool.releaseToGlobal(block);
        
        assertFalse(block.isInUse());
        assertEquals(10, pool.getFreeBlockCount());
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        SharedMemoryPool pool = new SharedMemoryPool(100, 1024);
        int threadCount = 10;
        int operationsPerThread = 20;
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    MemoryBlock block = pool.acquireFromGlobal();
                    assertNotNull(block);
                    pool.releaseToGlobal(block);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // All blocks should be returned
        assertTrue(pool.getFreeBlockCount() >= 100);
    }
    
    @Test
    void testNewBlockAllocation() {
        SharedMemoryPool pool = new SharedMemoryPool(0, 512);
        
        // Pool is empty, should allocate new block
        MemoryBlock block = pool.acquireFromGlobal();
        assertNotNull(block);
        assertEquals(512, block.getSize());
    }
    
    @Test
    void testMetrics() {
        SharedMemoryPool pool = new SharedMemoryPool(10, 1024);
        
        pool.acquireFromGlobal();
        pool.acquireFromGlobal();
        
        assertEquals(2, pool.getGlobalAcquires());
        
        MemoryBlock block = pool.acquireFromGlobal();
        pool.releaseToGlobal(block);
        
        assertEquals(3, pool.getGlobalAcquires());
        assertEquals(1, pool.getGlobalReleases());
    }
}

