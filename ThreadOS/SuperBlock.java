public class SuperBlock {
    private static int DEFAULT_INODE_BLOCKS = 64;
    private static int ERROR = -1;

    public int totalBlocks; 
    public int totalInodes; 
    public int freeList;   

    public SuperBlock(int diskSize) {
        byte[] block = new byte[Disk.blockSize];
        SysLib.rawread(0, block);
        
        totalBlocks = SysLib.bytes2int(block, 0);
        totalInodes = SysLib.bytes2int(block, 4);
        
        freeList = SysLib.bytes2int(block, 8);
        
        if(totalBlocks != diskSize || totalInodes < 1 || freeList < 2) {
            totalBlocks = diskSize;
            format(DEFAULT_INODE_BLOCKS);
        }
    }

    public void format(int inodes) {
        this.totalInodes = inodes;
        
        for(short i = 0; i < totalInodes; i++) {
            Inode inode = new Inode();
            inode.flag = 0; 
            inode.toDisk(i); 
        }
        
        freeList = 2 + (totalInodes * 32) / Disk.blockSize;
        
        for(int i = freeList; i < totalBlocks; i++) {
            byte buffer[] = new byte[Disk.blockSize];
            
            for(int j = 0; j < Disk.blockSize; j++) {
                buffer[j] = 0;
            }
            
            SysLib.int2bytes(i + 1, buffer, 0); 
            SysLib.rawwrite(i, buffer);
        }
    
        sync();    
    }

    public int getFreeBlock() {
        
        if(freeList < 0 || freeList > totalBlocks) {
            return ERROR;
        }

        byte buffer[] = new byte[Disk.blockSize];
        int freeBlock = freeList;

        SysLib.rawread(freeList, buffer);
        freeList = SysLib.bytes2int(buffer, 0);
        
        SysLib.int2bytes(0, buffer, 0);
        SysLib.rawwrite(freeBlock, buffer);

        return freeBlock;
    }

    public boolean returnBlock(int blockNumber) {
        if(blockNumber < 0 || blockNumber > totalBlocks) {
            return false;
        }

        byte buffer[] = new byte[Disk.blockSize];
        for(int i = 0; i < Disk.blockSize; i++)
        {
            buffer[i] = 0;           
        }
        
        SysLib.int2bytes(freeList, buffer, 0);
        SysLib.rawwrite(blockNumber, buffer);
       
        freeList = blockNumber;
        return true;
    }
    
    public void sync() {
        byte[] buffer = new byte[Disk.blockSize];
        
        SysLib.int2bytes(totalBlocks, buffer, 0);
        SysLib.int2bytes(totalInodes, buffer, 4);
        SysLib.int2bytes(freeList, buffer, 8);
        
        SysLib.rawwrite(0, buffer);
    }
}
