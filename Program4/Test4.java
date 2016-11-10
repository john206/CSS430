import java.util.*;

public class Test4 extends Thread {
    
    // Test4 members
    private static final int BLOCK_SIZE = 512;
    private static final int ARRAY_SIZE = 300;
    
    private static final int RANDOMIZE = 10;
    private static final int CHECK = 9;
    
    private int accessType;
    private boolean useCache;
    
    private String testType;
    
    private byte[] blockToWrite;    
    private byte[] blockToRead;
    
    private long beginRead;
    private long endRead;
    
    private long beginWrite;   
    private long endWrite;
    
    private Random rand;
    
    // Test4 constructor
    public Test4 (String args[]) {
        if (args.length != 2) {
            accessType = 1;
        }
        
        useCache = args[0].equals("enabled") ? true : false;        
        accessType = Integer.parseInt(args[1]);        
        
        blockToWrite = new byte[BLOCK_SIZE];        
        blockToRead = new byte[BLOCK_SIZE];
        
        rand = new Random();        
        rand.nextBytes(blockToWrite);
    }
    
    // Thread run method. Contains the main class logic.
    public void run() {
        SysLib.cout("\nTest 4 running...\n\n");
        
        SysLib.flush();
        
        switch (accessType) {
            case 1:
                randomAccesses();
                break;
            case 2:
                localizedAccesses();
                break;
            case 3:
                mixedAccesses();
                break;
            case 4:
                adversaryAccesses();
                break;
            case 5: // Convenience case to run all 4 tests at once
                randomAccesses();
                localizedAccesses();
                mixedAccesses();
                adversaryAccesses();
                break;            
            default:
                SysLib.cerr("Usage: Test4 [enabled|disabled] [1-5]");
                break;
        }
        
        sync();        
        SysLib.exit();
    }
    
    // Read with or without cache depending on command line arg
    private void read(int blockId, byte buffer[]) {
        if (useCache) {
            SysLib.cread(blockId, buffer);
        } else {
            SysLib.rawread(blockId, buffer);
        }
    }
    
    // Write with or without cache depending on command line arg
    private void write(int blockId, byte buffer[]) {
        if (useCache) {
            SysLib.cwrite(blockId, buffer);
        } else {
            SysLib.rawwrite(blockId, buffer);
        }
    }
    
    // Sync with or without cache depending on command line arg
    private void sync() {
        if (useCache) {
            SysLib.csync();
        } else {
            SysLib.sync();
        }
    }
    
    private void randomAccesses() {
        testType = "Random accesses";
        int[] randomAccessArray = new int[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            randomAccessArray[i] = (Math.abs(new Random().nextInt() % BLOCK_SIZE));
        }
        
        beginWrite = new Date().getTime();
        
        for (int i = 0; i < ARRAY_SIZE; i++) {
            write(randomAccessArray[i], blockToWrite);
        }
        
        endWrite = new Date().getTime();        
        beginRead = new Date().getTime();
        
        for (int i = 0; i < ARRAY_SIZE; i++) {
            read(randomAccessArray[i], blockToRead);
        }
        
        endRead = new Date().getTime();
        validateCache();
        displayAverage();
    }
   
    private void localizedAccesses() {            
        testType = "Localized accesses";
        beginWrite = new Date().getTime();
        
        for(int i = 0; i < 20; ++i) {            
            for(int j = 0; j < BLOCK_SIZE; ++j) {
                blockToWrite[j] = (byte)(i + j);
            }

            for(int k = 0; k < 1000; k += 100) {
                write(k, blockToWrite);
            }
        }
        endWrite = new Date().getTime();
        beginRead = new Date().getTime();
        
        for (int i = 0; i < 20; ++i) {
            for(int j = 0; j < 1000; j += 100) {
                read(j, blockToRead);                
            }
        }
        
        endRead = new Date().getTime();
        validateCache();
        displayAverage();
    }
    
    private void mixedAccesses() {
        testType = "Mixed accesses";
        int[] mixedAccessArray = new int[ARRAY_SIZE];
        
        for (int i = 0; i < ARRAY_SIZE; i++) {           
            if ((Math.abs(rand.nextInt() % RANDOMIZE)) < CHECK) {
                mixedAccessArray[i] = Math.abs(rand.nextInt() % RANDOMIZE);
            } else {
                mixedAccessArray[i] = (Math.abs(rand.nextInt() % BLOCK_SIZE));
            }
        }
        
        beginWrite = new Date().getTime();
        
        for (int i = 0; i < ARRAY_SIZE; i++) {
            write(mixedAccessArray[i], blockToWrite);
        }
      
        endWrite = new Date().getTime();        
        beginRead = new Date().getTime();
        
        for (int i = 0; i < ARRAY_SIZE; i++) {
            read(mixedAccessArray[i], blockToRead);
        }
        
        endRead = new Date().getTime();
        validateCache();
        displayAverage();
    }
    
    private void adversaryAccesses() {
        
        testType = "Adversary accesses";        
        beginWrite = new Date().getTime();
        
        for (int i = 0; i < BLOCK_SIZE; i++) {            
            write(i, blockToWrite);
        }
       
        endWrite = new Date().getTime();       
        beginRead = new Date().getTime();
       
        for (int i = 0; i < BLOCK_SIZE; i++) {           
            read(i, blockToRead);
        }
       
        endRead = new Date().getTime();
        validateCache();      
        displayAverage();
    }   
   
    private void displayAverage() {
        SysLib.cout(testType + " with cache " + (useCache ? "enabled" : "disabled") + "\n");        
        SysLib.cout("average write: " + (endWrite - beginWrite) + "\n");
        SysLib.cout("average read: " + (endRead - beginRead) +" \n\n");        
    }
    
    private void validateCache() {
        if (!Arrays.equals(blockToWrite, blockToRead)) {
            SysLib.cout("ERROR: Cache not valid\n");
        }
    }
}