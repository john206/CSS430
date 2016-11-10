import java.util.*;

public class Cache { 
        
    // Data structure used to contain an entry in a page table   
    private class CacheEntry {        
        
        // CacheEntry member variables
        private byte[] frameData; 
        private int frameID;  
        private boolean referenceBit;     
        private boolean dirtyBit;         
        
        // Constructor for CacheEntry
        private CacheEntry(int blockSize) {
            frameData = new byte[blockSize]; 
            frameID = INVALID;
            referenceBit = false;
            dirtyBit = false;
        }
    }
    
    // Cache member variables
    private CacheEntry[] pageTable;
    private int blockSize;   
    private int victimID;
    private static final int INVALID = -1;
        
    // Constructor for Cache
    public Cache(int blockSize, int cacheBlocks) {
        pageTable = new CacheEntry[cacheBlocks]; 
        this.blockSize = blockSize; 
        victimID = 0;
        
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = new CacheEntry(blockSize);
        }
    }    
    
    // Reads into the buffer[] array the cache block specified by blockID from the disk cache if it is in cache,
    // otherwise reads the corresponding disk block from the disk device. Upon an error, it returns false.
    public synchronized boolean read(int blockID, byte buffer[]) {
        if (blockID < 0) { 
            return false;
        }
        
        int frame = findFrame(blockID); 
        
        if (frame != INVALID) { 
            readHelper(frame, blockID, buffer);
            return true;
        }
        
        frame = findFrame(INVALID); 
        
        if (frame != INVALID) { 
            SysLib.rawread(blockID, pageTable[frame].frameData);
            readHelper(frame, blockID, buffer);
            return true;
        }
        
        findVictim();
        
        // Save the victim to disk if it's dirty
        diskWrite(victimID);
        
        // Copy data into the Cache
        SysLib.rawread(blockID, pageTable[victimID].frameData);
        
        // Load the buffer
        readHelper(victimID, blockID, buffer);
        return true;
    }
    
    // Writes the buffer[] array contents to the cache block specified by blockID from the disk cache 
    // if it is in cache, otherwise finds a free cache block and writes the buffer[] contents on it. 
    // No write through. Upon an error, it returns false.
    public synchronized boolean write(int blockID, byte buffer[]) {
        if (blockID < 0) { 
            return false;
        }
        
        int frame = findFrame(blockID); 
        
        if (frame != INVALID) { 
            writeHelper(frame, blockID, buffer); 
            return true;
        }
        
        frame = findFrame(INVALID); 
        
        if (frame != INVALID) { 
            writeHelper(frame, blockID, buffer);
            return true;
        }
        
        findVictim();
         
        diskWrite(victimID); 
        writeHelper(victimID, blockID, buffer);
        return true;
    }
    
    // Writes back all dirty blocks to Disk.java and therefater forces Diskjava to write back all contents 
    // to the DISK file. 
    public synchronized void sync() {
        for (int i = 0; i < pageTable.length; i++) {
            diskWrite(i);
        }
        SysLib.sync();
    }
    
    // Same as syc but also invalidates all cached blocks
    public synchronized void flush() {
        for (int i = 0; i < pageTable.length; i++) { 
            diskWrite(i);
            pageTable[i].frameID = INVALID;  
            pageTable[i].referenceBit = false;             
        }
        SysLib.sync();
    }
    
    // Second Chance Algorithm. Loops through the page table to find a victim whose reference 
    // and dirty bits are not set. If no victim is found, the algorithm proceeds to look for a 
    // victim whose reference bit not set but whose dirty bit is set. If still none are found, 
    // the algorithm resets the last reference bit and starts over, eventually returning a victim.
    private void findVictim() {
        while (true) {
            for (int i = 0; i < pageTable.length; i++) {
                if (!pageTable[victimID].referenceBit && !pageTable[victimID].dirtyBit) {
                    return;
                }
                victimID = (victimID + 1) % (pageTable.length);
            }
            for (int i = 0; i < pageTable.length; i++) {
                if (!pageTable[victimID].referenceBit && pageTable[victimID].dirtyBit) {
                    return;
                }
                pageTable[victimID].referenceBit = false;
                victimID = (victimID + 1) % (pageTable.length);
            }
        }
    }
    
    // Loops through the page table looking for a target blockID
    private int findFrame(int blockID) {
        for (int i = 0; i < pageTable.length; i++) {
            if (pageTable[i].frameID == blockID) {
                return i;
            }
        }
        return INVALID;
    }
    
    // Writes a CacheEntry to disk before removing it from the Cache.
    private void diskWrite(int victim) {
        if (pageTable[victim].dirtyBit && pageTable[victim].frameID != INVALID) {
            SysLib.rawwrite(pageTable[victim].frameID, pageTable[victim].frameData);
            pageTable[victim].dirtyBit = false; 
        }
    }
    
    // Helper function for write. Moves repetitive code into one block.
    private void writeHelper(int frame, int blockID, byte buffer[]) {
        pageTable[frame].frameData = Arrays.copyOfRange(buffer, 0, blockSize);
        pageTable[frame].dirtyBit = true; 
        pageTable[frame].frameID = blockID; 
        pageTable[frame].referenceBit = true;
    } 
    
    // Helper function for read. Moves repetitive code into one block.
    private void readHelper(int frame, int blockID, byte buffer[]) {
        System.arraycopy(pageTable[frame].frameData, 0, buffer, 0, blockSize);
        pageTable[frame].frameID = blockID;   
        pageTable[frame].referenceBit = true;
    }
}