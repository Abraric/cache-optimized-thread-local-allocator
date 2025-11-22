package com.ctma.allocator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AllocatorConfig.
 */
class AllocatorConfigTest {
    
    @Test
    void testValidConfig() {
        AllocatorConfig config = new AllocatorConfig(1024, 100, 50);
        assertEquals(1024, config.getBlockSizeBytes());
        assertEquals(100, config.getInitialGlobalBlocks());
        assertEquals(50, config.getInitialPerThreadBlocks());
    }
    
    @Test
    void testBuilder() {
        AllocatorConfig config = AllocatorConfig.builder()
                .blockSizeBytes(2048)
                .initialGlobalBlocks(200)
                .initialPerThreadBlocks(100)
                .build();
        
        assertEquals(2048, config.getBlockSizeBytes());
        assertEquals(200, config.getInitialGlobalBlocks());
        assertEquals(100, config.getInitialPerThreadBlocks());
    }
    
    @Test
    void testBuilderDefaults() {
        AllocatorConfig config = AllocatorConfig.builder().build();
        assertTrue(config.getBlockSizeBytes() > 0);
        assertTrue(config.getInitialGlobalBlocks() >= 0);
        assertTrue(config.getInitialPerThreadBlocks() >= 0);
    }
    
    @Test
    void testInvalidBlockSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AllocatorConfig(0, 100, 50);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new AllocatorConfig(-1, 100, 50);
        });
    }
    
    @Test
    void testInvalidGlobalBlocks() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AllocatorConfig(1024, -1, 50);
        });
    }
    
    @Test
    void testInvalidPerThreadBlocks() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AllocatorConfig(1024, 100, -1);
        });
    }
}

