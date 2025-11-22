package com.ctma.allocator;

/**
 * Immutable configuration class for the thread-local memory allocator.
 * 
 * <p>This class holds all configuration parameters needed to initialize
 * the allocator, including block sizes and pool capacities.
 * 
 * @author CTMA Project
 */
public final class AllocatorConfig {
    private final int blockSizeBytes;
    private final int initialGlobalBlocks;
    private final int initialPerThreadBlocks;
    
    /**
     * Creates a new AllocatorConfig with the specified parameters.
     * 
     * @param blockSizeBytes size of each memory block in bytes (must be > 0)
     * @param initialGlobalBlocks initial number of blocks in the global pool (must be >= 0)
     * @param initialPerThreadBlocks initial number of blocks per thread-local pool (must be >= 0)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public AllocatorConfig(int blockSizeBytes, int initialGlobalBlocks, int initialPerThreadBlocks) {
        if (blockSizeBytes <= 0) {
            throw new IllegalArgumentException("Block size must be greater than 0");
        }
        if (initialGlobalBlocks < 0) {
            throw new IllegalArgumentException("Initial global blocks must be non-negative");
        }
        if (initialPerThreadBlocks < 0) {
            throw new IllegalArgumentException("Initial per-thread blocks must be non-negative");
        }
        
        this.blockSizeBytes = blockSizeBytes;
        this.initialGlobalBlocks = initialGlobalBlocks;
        this.initialPerThreadBlocks = initialPerThreadBlocks;
    }
    
    /**
     * Creates a builder for constructing AllocatorConfig instances.
     * 
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Returns the size of each memory block in bytes.
     * 
     * @return block size in bytes
     */
    public int getBlockSizeBytes() {
        return blockSizeBytes;
    }
    
    /**
     * Returns the initial number of blocks in the global pool.
     * 
     * @return initial global blocks count
     */
    public int getInitialGlobalBlocks() {
        return initialGlobalBlocks;
    }
    
    /**
     * Returns the initial number of blocks per thread-local pool.
     * 
     * @return initial per-thread blocks count
     */
    public int getInitialPerThreadBlocks() {
        return initialPerThreadBlocks;
    }
    
    /**
     * Builder class for AllocatorConfig.
     */
    public static class Builder {
        private int blockSizeBytes = 1024; // Default: 1KB
        private int initialGlobalBlocks = 1000; // Default: 1000 blocks
        private int initialPerThreadBlocks = 100; // Default: 100 blocks per thread
        
        /**
         * Sets the block size in bytes.
         * 
         * @param blockSizeBytes block size (must be > 0)
         * @return this builder
         */
        public Builder blockSizeBytes(int blockSizeBytes) {
            this.blockSizeBytes = blockSizeBytes;
            return this;
        }
        
        /**
         * Sets the initial number of blocks in the global pool.
         * 
         * @param initialGlobalBlocks initial global blocks (must be >= 0)
         * @return this builder
         */
        public Builder initialGlobalBlocks(int initialGlobalBlocks) {
            this.initialGlobalBlocks = initialGlobalBlocks;
            return this;
        }
        
        /**
         * Sets the initial number of blocks per thread-local pool.
         * 
         * @param initialPerThreadBlocks initial per-thread blocks (must be >= 0)
         * @return this builder
         */
        public Builder initialPerThreadBlocks(int initialPerThreadBlocks) {
            this.initialPerThreadBlocks = initialPerThreadBlocks;
            return this;
        }
        
        /**
         * Builds the AllocatorConfig instance.
         * 
         * @return a new AllocatorConfig with the configured values
         * @throws IllegalArgumentException if any parameter is invalid
         */
        public AllocatorConfig build() {
            return new AllocatorConfig(blockSizeBytes, initialGlobalBlocks, initialPerThreadBlocks);
        }
    }
    
    @Override
    public String toString() {
        return String.format("AllocatorConfig[blockSize=%d, globalBlocks=%d, perThreadBlocks=%d]",
                blockSizeBytes, initialGlobalBlocks, initialPerThreadBlocks);
    }
}

