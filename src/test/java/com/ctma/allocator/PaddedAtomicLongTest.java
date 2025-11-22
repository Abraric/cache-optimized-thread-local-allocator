package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaddedAtomicLong.
 */
class PaddedAtomicLongTest {
    
    @Test
    void testDefaultConstructor() {
        PaddedAtomicLong padded = new PaddedAtomicLong();
        assertEquals(0L, padded.get());
    }
    
    @Test
    void testInitialValue() {
        PaddedAtomicLong padded = new PaddedAtomicLong(42L);
        assertEquals(42L, padded.get());
    }
    
    @Test
    void testSet() {
        PaddedAtomicLong padded = new PaddedAtomicLong();
        padded.set(100L);
        assertEquals(100L, padded.get());
    }
    
    @Test
    void testIncrementAndGet() {
        PaddedAtomicLong padded = new PaddedAtomicLong(0L);
        assertEquals(1L, padded.incrementAndGet());
        assertEquals(1L, padded.get());
        assertEquals(2L, padded.incrementAndGet());
    }
    
    @Test
    void testGetAndIncrement() {
        PaddedAtomicLong padded = new PaddedAtomicLong(0L);
        assertEquals(0L, padded.getAndIncrement());
        assertEquals(1L, padded.get());
    }
    
    @Test
    void testAddAndGet() {
        PaddedAtomicLong padded = new PaddedAtomicLong(10L);
        assertEquals(15L, padded.addAndGet(5L));
        assertEquals(15L, padded.get());
    }
    
    @Test
    void testGetAndAdd() {
        PaddedAtomicLong padded = new PaddedAtomicLong(10L);
        assertEquals(10L, padded.getAndAdd(5L));
        assertEquals(15L, padded.get());
    }
}

