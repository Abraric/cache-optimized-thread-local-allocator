package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MemoryBlock.
 */
class MemoryBlockTest {
    
    @Test
    void testCreation() {
        MemoryBlock block = new MemoryBlock(1L, 1024);
        assertEquals(1L, block.getId());
        assertEquals(1024, block.getSize());
        assertFalse(block.isInUse());
        assertNotNull(block.getBuffer());
        assertEquals(1024, block.getBuffer().length);
    }
    
    @Test
    void testMarkUsed() {
        MemoryBlock block = new MemoryBlock(1L, 512);
        assertFalse(block.isInUse());
        block.markUsed();
        assertTrue(block.isInUse());
    }
    
    @Test
    void testMarkFree() {
        MemoryBlock block = new MemoryBlock(1L, 512);
        block.markUsed();
        assertTrue(block.isInUse());
        block.markFree();
        assertFalse(block.isInUse());
    }
    
    @Test
    void testToString() {
        MemoryBlock block = new MemoryBlock(42L, 256);
        String str = block.toString();
        assertTrue(str.contains("42"));
        assertTrue(str.contains("256"));
    }
}

