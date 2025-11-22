package com.ctma.allocator;

/**
 * Represents a unit of memory (simulated) in the allocator.
 * This is a conceptual memory block that wraps a byte array buffer.
 * 
 * <p>In a native implementation, this would represent an actual memory region.
 * Here, we simulate it using a byte array to model allocation behavior.
 * 
 * @author CTMA Project
 */
public class MemoryBlock {
    private final long id;
    private final byte[] buffer;
    private volatile boolean inUse;
    
    /**
     * Creates a new MemoryBlock with the specified ID and buffer size.
     * 
     * @param id unique identifier for this block
     * @param bufferSize size of the buffer in bytes
     */
    public MemoryBlock(long id, int bufferSize) {
        this.id = id;
        this.buffer = new byte[bufferSize];
        this.inUse = false;
    }
    
    /**
     * Marks this block as in use.
     */
    public void markUsed() {
        this.inUse = true;
    }
    
    /**
     * Marks this block as free (available for reuse).
     */
    public void markFree() {
        this.inUse = false;
    }
    
    /**
     * Returns whether this block is currently in use.
     * 
     * @return true if in use, false otherwise
     */
    public boolean isInUse() {
        return inUse;
    }
    
    /**
     * Returns the unique identifier of this block.
     * 
     * @return the block ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Returns the buffer associated with this block.
     * 
     * @return the byte array buffer
     */
    public byte[] getBuffer() {
        return buffer;
    }
    
    /**
     * Returns the size of the buffer in bytes.
     * 
     * @return buffer size
     */
    public int getSize() {
        return buffer.length;
    }
    
    @Override
    public String toString() {
        return String.format("MemoryBlock[id=%d, size=%d, inUse=%s]", 
                id, buffer.length, inUse);
    }
}

