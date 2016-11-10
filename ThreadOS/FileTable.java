import java.util.Vector;

public class FileTable {
    private static final int ERROR = -1;
    
    private Vector table; 
    private Directory dir; 

    public FileTable(Directory directory) {
        table = new Vector(); 
        dir = directory; 
    }

    public synchronized FileTableEntry falloc(String filename, String mode) {
        short iNumber = ERROR;
        Inode inode = null;
        
        while(true) {
            
            if(filename.equals("/")) {
                iNumber = 0;
            }
            else {
                iNumber = dir.namei(filename);
            }
            
            if (iNumber < 0) {
                if (mode.compareTo("r") == 0) {
                    return null;
                }
                
                if ((iNumber = dir.ialloc(filename)) < 0) {
                    return null;
                }
                
                inode = new Inode();
                break;
            }
            
            inode = new Inode(iNumber); 
            
            if (inode.flag == 3) {
                iNumber = ERROR; 
                return null;
            }
           
            if (inode.flag == 0 || inode.flag == 1) {
                break;
            }
            
            if (mode.compareTo("r") == 0 && inode.flag == 2) {
                break;
            }
           
            try {
                wait();
            } 
            catch (InterruptedException ie) { }
        }
        
        inode.count++;
        inode.toDisk(iNumber);
        
        FileTableEntry ftEnt = new FileTableEntry(inode, iNumber, mode);
        table.addElement(ftEnt);
        
        return ftEnt;
    }
    
    public synchronized boolean ffree(FileTableEntry ftEnt) {
        if(!table.removeElement(ftEnt)) {
            return false;
        }
        
        if (ftEnt.inode.flag == 1 || ftEnt.inode.flag == 2) {
            ftEnt.inode.flag = 0;
        } 
        else if (ftEnt.inode.flag == 4 || ftEnt.inode.flag == 5) {
            ftEnt.inode.flag = 3;
        }
        
        ftEnt.inode.count--;
        ftEnt.inode.toDisk(ftEnt.iNumber);
        ftEnt = null;
        
        notify();        
        return true;       
    }
    
    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}
