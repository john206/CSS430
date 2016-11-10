

public class Inode
{
    
     
    public int totalsize;  
    public short user;
    public short indirect;  
    public short currentsize;
    public final static int directSize = 11;                
    public static final short NULL = -1;                    
    public short[] directPtrs = new short[directSize];    

    Inode(){
        user = 0;
        totalsize= 0;
        currentsize = 0;
        for (int i = 0; i < directSize; i++){
            directPtrs[i] = -1;
        }
        indirect = -1;
    }

    Inode(short inum){

        int block = getBlockNum(inum);      
            
        int iNodeID = inum % 16;    
        int offset = iNodeID * 32;             
        
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(block, data);

        totalsize = SysLib.bytes2int(data, offset);
        currentsize = SysLib.bytes2short(data, offset +=4);
        user = SysLib.bytes2short(data, offset += 2);

        for (int i = 0; i < directSize; i++){
            directPtrs[i] = SysLib.bytes2short(data, offset += 2);
        }
  
        indirect = SysLib.bytes2short(data, offset += 2);
    }

    public int setTargetBlock(int sPointer, short tempBlock){
        int pointer = sPointer / Disk.blockSize;  
        if(pointer < directSize ) {
            if(directPtrs[pointer] >= 0) {
                return NULL;
            }
            if(pointer > 0 && directPtrs[pointer - 1] == -1) {
                return -2;
            }
            directPtrs[pointer] = tempBlock;
            return 0;
        }
        if(indirect < 0) {
            return -3;
        }
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
       
        int offsetPtr = pointer - directSize;
        if(SysLib.bytes2short(data, offsetPtr * 2) > 0) {
            return NULL;
        }
        SysLib.short2bytes(tempBlock, data, offsetPtr * 2);
        SysLib.rawwrite(indirect, data);
        return 0;
    }

    public int findTargetBlock(int sPointer){
        int pointer = sPointer / Disk.blockSize;  
        if (pointer < directSize) {
            return directPtrs[pointer];
        }
     
        if(indirect < 0 ) {
            return NULL;
        }
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
        return SysLib.bytes2short(data, (pointer - directSize) * 2);
    }

    public byte[] unregisterIndexBlock(){
        
        if(indirect == NULL) {
            return null;
        }
       
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
        indirect = NULL;
        return data;
    }

    public boolean setIndexBlock(short inum){
        
        for(int i = 0; i < directSize; i++) {
            if(directPtrs[i] != NULL) {
                continue;
            }
            return false;
        }
    
        if(this.indirect != NULL) {
            return false;
        }
        indirect = inum;
        byte[] data = new byte[Disk.blockSize];
        for(int i = 0; i < Disk.blockSize / 2; i++){
            SysLib.short2bytes(NULL, data, i * 2);
        }
        
        SysLib.rawwrite(inum, data);
        return true;
    }

    public int getBlockNum(int inum){
        return (inum / 16 + 1);
    }

    public void toDisk(short inum){
        int block = getBlockNum(inum);                       
        int iNodeID = inum % 16;              
        int offset = iNodeID * 32;                       
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(block, data);
        SysLib.int2bytes(totalsize, data, offset);                 
        SysLib.short2bytes(currentsize, data, offset += 4);    
        SysLib.short2bytes(user, data, offset += 2);   
        for (int i = 0; i < directSize; i++){
            SysLib.short2bytes(directPtrs[i], data, offset += 2);
        }

        SysLib.short2bytes(indirect, data, offset += 2);
        SysLib.rawwrite(block, data); 
    }


    public byte[] deleteIndirectPointer(){
        if(indirect == -1){
            return null;
        }
        byte[] data = new byte[Disk.blockSize];
        SysLib.rawread(indirect, data);
        indirect = -1;
        return data;
    }
}
