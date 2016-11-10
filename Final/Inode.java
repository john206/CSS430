public class Inode {                     
    private static final int SIZEOF_INODE = 32;
    private static final int INODES_PER_BLOCK = 16;                  
    private static final int SIZEOF_SHORT = 2;                 
    private static final int SIZEOF_INT = 4;           
    public static final short ERROR = -1;  
    public static final int directSize = 11;
    
    public int length;                                      
    public short count;
    public short[] direct = new short[directSize];      
    public short indirect;
    public short flag;
 
    public Inode() {
        length = 0;
        count = 0;
        flag = 0;
        for (int i = 0; i < directSize; i++) {
            direct[i] = ERROR;
        }
        
        indirect = ERROR;
    }
    
    public Inode(short iNumber) {
        int block = iNumber / INODES_PER_BLOCK + 1;                       
        int iNodeID = iNumber % INODES_PER_BLOCK;               
        int offset = iNodeID * SIZEOF_INODE;                       
        
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(block, data);

        length = SysLib.bytes2int(data, offset);
        count = SysLib.bytes2short(data, offset += SIZEOF_INT);
        flag = SysLib.bytes2short(data, offset += SIZEOF_SHORT);

        for (int i = 0; i < directSize; i++) {
            direct[i] = SysLib.bytes2short(data, offset += SIZEOF_SHORT);
        }
        
        indirect = SysLib.bytes2short(data, offset += SIZEOF_SHORT);
    }
        
    public int findTargetBlock(int offset) {
        int blockPtr = offset / Disk.blockSize;  
        
        if (blockPtr < directSize) {
            return direct[blockPtr];
        }
       
        if(indirect < 0 ) {
            return ERROR;
        }
        
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
              
        return SysLib.bytes2short(data, (blockPtr - directSize) * SIZEOF_SHORT);
    }
    
    public int setTargetBlock(int offset, short targetBlock) {
        int blockPtr = offset / Disk.blockSize;  
        
        if(blockPtr < directSize ) {
            if(direct[blockPtr] >= 0) {
                return ERROR;
            }
            if(blockPtr > 0 && direct[blockPtr - 1] == -1) {
                return -2;
            }
            direct[blockPtr] = targetBlock;
            return 0;
        }
        
        if(indirect < 0) {
            return -3;
        }
        
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
       
        int offsetPtr = blockPtr - directSize;
        
        if(SysLib.bytes2short(data, offsetPtr * SIZEOF_SHORT) > 0) {
            return ERROR;
        }
        
        SysLib.short2bytes(targetBlock, data, offsetPtr * SIZEOF_SHORT);
        SysLib.rawwrite(indirect, data);
        
        return 0;
    }
    
    public byte[] freeIndirectBlock() {
        if(indirect == ERROR) {
            return null;
        }
        
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
        indirect = ERROR;
        
        return data;
    }
    
    public boolean setIndexBlock(short iNumber) {
        for(int i = 0; i < directSize; i++) {
            if(direct[i] == ERROR) {
                return false;
            }       
        }
    
        if(this.indirect != ERROR) {
            return false;
        }
        
        indirect = iNumber;
        byte[] data = new byte[Disk.blockSize];
        for(int i = 0; i < Disk.blockSize / 2; i++)
        {
            SysLib.short2bytes(ERROR, data, i * SIZEOF_SHORT);
        }
        
        SysLib.rawwrite(iNumber, data);
        return true;
    }
    
    public void toDisk(short iNumber) {
        int block = iNumber / INODES_PER_BLOCK + 1;                       
        int iNodeID = iNumber % INODES_PER_BLOCK;              
        int offset = iNodeID * SIZEOF_INODE;                       

        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(block, data);

        SysLib.int2bytes(length, data, offset);                 
        
        SysLib.short2bytes(count, data, offset += SIZEOF_INT);    
        SysLib.short2bytes(flag, data, offset += SIZEOF_SHORT);   

        for (int i = 0; i < directSize; i++) {
            SysLib.short2bytes(direct[i], data, offset += SIZEOF_SHORT);
        }        
        
        SysLib.short2bytes(indirect, data, offset += SIZEOF_SHORT);
        SysLib.rawwrite(block, data);                            
    }
}
