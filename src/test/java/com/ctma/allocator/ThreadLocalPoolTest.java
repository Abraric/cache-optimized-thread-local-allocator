package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThreadLocalPool.
 */
class ThreadLocalPoolTest {
    
    @Test
    void testAllocate() {
        ThreadLocalPool pool = new ThreadLocalPool(10, 1024, 0);
        MemoryBlock block = pool.allocate();
        
        assertNotNull(block);
        assertTrue(block.isInUse());
        assertEquals(9, pool.getFreeBlockCount());
    }
    
    @Test
    void testDeallocate() {
        ThreadLocalPool pool = new ThreadLocalPool(10, 1024, 0);
        MemoryBlock block = pool.allocate();
        pool.deallocate(block);
        
        assertFalse(block.isInUse());
        assertEquals(10, pool.getFreeBlockCount());
    }
    
    @Test
    void testExhaustion() {
        ThreadLocalPool pool = new ThreadLocalPool(5, 1024, 0);
        
        // Allocate all blocks
        for (int i = 0; i < 5; i++) {
            assertNotNull(pool.allocate());
        }
        
        // Next allocation should return null
        assertNull(pool.allocate());
        assertEquals(0, pool.getFreeBlockCount());
    }
    
    @Test
    void testReuse() {
        ThreadLocalPool pool = new ThreadLocalPool(3, 1024, 0);
        
        MemoryBlock block1 = pool.allocate();
        long id1 = block1.getId();
        pool.deallocate(block1);
        
        MemoryBlock block2 = pool.allocate();
        assertEquals(id1, block2.getId()); // Should reuse the same block
    }
    
    @Test
    void testMetrics() {
        ThreadLocalPool pool = new ThreadLocalPool(10, 1024, 0);
        
        pool.allocate();
        pool.allocate();
        pool.allocate();
        
        assertEquals(3, pool.getLocalAllocations());
        assertEquals(3, pool.getHits());
        assertEquals(0, pool.getMisses());
        
        MemoryBlock block = pool.allocate();
        pool.deallocate(block);
        assertEquals(1, pool.getLocalFrees());
    }
    
    @Test
    void testHitRate() {
        ThreadLocalPool pool = new ThreadLocalPool(5, 1024, 0);
        
        for (int i = 0; i < 5; i++) {
            pool.allocate();
        }
        pool.allocate(); // This should be a miss
        
        double hitRate = pool.getHitRate();
        assertEquals(83.33, hitRate, 0.1); // 5 hits out of 6 attempts
    }
}

