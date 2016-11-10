public class FileSystem {
    private SuperBlock superblock;             
    private Directory directory;                
    private FileTable filetable;		
    
    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;
    private static final int ERROR = -1;        
    
    public FileSystem(int diskBlocks) {
        
        superblock = new SuperBlock(diskBlocks);
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
       
        FileTableEntry ftEnt = open("/", "r");
        int dirSize = fsize(ftEnt);
        
        if(dirSize > 0) {         
            byte[] dirData = new byte[dirSize];
            read(ftEnt, dirData);            
            directory.bytes2directory(dirData);
        }
        close(ftEnt);
    }
    
    public boolean format(int files) {
        if(!filetable.fempty()) {
            return false;
        }
        superblock.format(files);        
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        	
        return true;
    }
    
    public FileTableEntry open(String filename, String mode) {
        FileTableEntry ftEnt = filetable.falloc(filename, mode);
        
        if(mode == "w") {
            if (!deallocAllBlocks(ftEnt)) {
                return null;
            }
        }
        return ftEnt;
    }
    
    public boolean close(FileTableEntry ftEnt) {
        synchronized(ftEnt) {
            ftEnt.count--;
            if(ftEnt.count > 0) {
                return true;
            }
        }
        
        return filetable.ffree(ftEnt);
    }
    
    public boolean delete(String filename) {
        FileTableEntry ftEnt = open(filename, "r");
        if(ftEnt == null) {	
            return false;
        }
        
        short num = ftEnt.iNumber;
        if(close(ftEnt)) {	          
            return directory.ifree(num);	
        }

        return false;
    }
    
    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        synchronized(ftEnt) {
            switch(whence) {	    
                case SEEK_SET: {                    
                    if(offset < 0 || offset > fsize(ftEnt)) {
                        return ERROR;
                    }
                    
                    ftEnt.seekPtr = offset; 
                    break;
                }
                
                case SEEK_CUR: {                    
                    if(offset < 0 || offset > fsize(ftEnt)) {
                        return -1;
                    }
                   
                    ftEnt.seekPtr += offset;
                    break;
                }
                
                case SEEK_END: {	
                    ftEnt.seekPtr = fsize(ftEnt) + offset;
                    break;
                }
                
                default: {
                    break;
                }
            }
        }
        
        return ftEnt.seekPtr;
    }
    
    public int read(FileTableEntry ftEnt, byte[] dirData) {
        
        if(ftEnt.mode == "w" || ftEnt.mode == "a") {
            return ERROR;
        }
        
        synchronized(ftEnt) {
            int bytesRead = 0;	            
            int bytesRemaining = dirData.length;	
            
            while(bytesRemaining > 0 && ftEnt.seekPtr < fsize(ftEnt)) {
                int currentBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);	
                
                if(currentBlock > ERROR) {
                    int currentPtr = ftEnt.seekPtr;
                    int size = fsize(ftEnt);
                    byte[] buffer = new byte[Disk.blockSize];
                    SysLib.rawread(currentBlock, buffer);
                  
                    int offset = currentPtr % Disk.blockSize;
                    int bytesRemainingInTable = size - currentPtr;
                    
                    int bytesRemainingInBlock = Disk.blockSize - offset;
                    int currentRead = Math.min(bytesRemaining, Math.min(bytesRemainingInBlock, bytesRemainingInTable));
                    
                    System.arraycopy(buffer, offset, dirData, bytesRead, currentRead);
                    
                    bytesRead += currentRead;
                    bytesRemaining -= currentRead;
                    ftEnt.seekPtr += currentRead;
                }
                else {
                    break;
                }
            }
            return bytesRead;
        }
    }
    
    public int write(FileTableEntry ftEnt, byte[] dirData) {
        if (ftEnt.mode == "r") {
            return ERROR;
        }
        
        synchronized(ftEnt) {
            int bytesWritten = 0; 
            int bytesRemainingToWrite = dirData.length; 
            while(bytesRemainingToWrite > 0) {
                short currentPosition; 
                
                int currentBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                
                if(currentBlock == Inode.ERROR) {
                    short freeBlock = (short) superblock.getFreeBlock();
                    
                    switch(ftEnt.inode.setTargetBlock(ftEnt.seekPtr, freeBlock)) {
                        case 0: {    
                            break;
                        }
                        
                        case -1:
                        case -2: {
                            return ERROR;
                        }
                          
                        case -3: {
                            currentPosition = (short) superblock.getFreeBlock();
                            if(!ftEnt.inode.setIndexBlock(currentPosition)) {
                                return ERROR;
                            }
                            
                            if(ftEnt.inode.setTargetBlock(ftEnt.seekPtr, freeBlock) == 0) {
                                break;
                            }
                            
                            return ERROR;
                        }
                    }
                    
                    currentBlock = freeBlock;
                }
                
                byte[] buffer = new byte[Disk.blockSize];
                SysLib.rawread(currentBlock, buffer);
                
                currentPosition = (short) (ftEnt.seekPtr % Disk.blockSize);
                int bytesRemainingInBlock = Disk.blockSize - currentPosition;
                int writeSize = Math.min(bytesRemainingInBlock, bytesRemainingToWrite);
                
                System.arraycopy(dirData, bytesWritten, buffer, currentPosition,writeSize);
                SysLib.rawwrite(currentBlock, buffer);
                
                bytesWritten += writeSize;
                bytesRemainingToWrite -= writeSize;
                ftEnt.seekPtr += writeSize;
                
                if(ftEnt.seekPtr > ftEnt.inode.length) {
                    ftEnt.inode.length = ftEnt.seekPtr;
                }
            }
            
            ftEnt.inode.toDisk(ftEnt.iNumber);            
            return bytesWritten;
        }
    }
   
    public int fsize(FileTableEntry ftEnt) {
        synchronized(ftEnt) {
            return ftEnt.inode.length;
        }
    }
    
    private boolean deallocAllBlocks(FileTableEntry ftEnt) {
        if(ftEnt.inode.count == 0) {
            return false;
        }
        
        byte[] buffer = ftEnt.inode.freeIndirectBlock();
        
        if(buffer != null) {
            int block = SysLib.bytes2short(buffer, 0);
            if(block != -1) {
                superblock.returnBlock(block);
            }
        }
        
        for(int i = 0; i < Inode.directSize; i++) {
            if(ftEnt.inode.direct[i] != -1) {
                superblock.returnBlock(ftEnt.inode.direct[i]);
                ftEnt.inode.direct[i] = -1;
            }
        }
        
        ftEnt.inode.toDisk(ftEnt.iNumber);
        return true;
    }
    
    public void sync() {
        FileTableEntry ftEnt = open("/", "w");       
        byte[] buffer = directory.directory2bytes();
        	
        write(ftEnt, buffer);	
        close(ftEnt);
        		
        superblock.sync();
    }
}