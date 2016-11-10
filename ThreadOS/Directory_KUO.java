
public class Directory {
   
    private int filesizes[];              
    private char filenames[][]; 
    public Directory(int max) {   
        filesizes = new int[max];    
        for(int i = 0; i < max; i++) {
            filesizes[i] = 0;               
        }
    
        filenames = new char[max][30];
        String root = "/";               
        filesizes[0] = root.length();       
        root.getChars(0, filesizes[0], filenames[0], 0);  
    }
    
    public byte[] directory2bytes() {
        byte directoryInfo[] = new byte[filesizes.length * 4 + filenames.length * 30 * 2];
        int off= 0;
        
        for (int i = 0; i < filesizes.length; i++, off += 4) {
            SysLib.int2bytes(filesizes[i], directoryInfo, off);
        }
        
        for (int i = 0; i < filenames.length; i++, off += 30 * 2) {
            String fname = new String(filenames[i], 0, filesizes[i]);
            byte fileInfo[] = fname.getBytes();
            
            for(int j = 0; j < fileInfo.length; j++) {
                directoryInfo[j + off] = fileInfo[j];
            }
        }
        
        return directoryInfo;
    }

    public void bytes2directory(byte data[]) {
        int off = 0;

        for (int i = 0; i < filesizes.length; i++, off += 4) {
            filesizes[i] = SysLib.bytes2int(data, off);
        }
    
        for (int i = 0; i < filenames.length; i++, off += 30 * 2) {
            String fname = new String(data, off, 30 * 2);
            fname.getChars(0, filesizes[i], filenames[i], 0);
        }
    }

    public short ialloc(String filename) {
        for (short i = 1; i < filesizes.length; i++) {
            if(filesizes[i] == 0) { 
                int s = filename.length();
                if(s > 30) { 
                    s = 30;
                }
                
                filesizes[i] = s;
                filename.getChars(0, filesizes[i], filenames[i], 0); 
                return i; 
            }
        }
        
        return -1;
        
    }
    public short namei(String filename) {
        for(short i = 0; i < filesizes.length; i++) {
            if(filesizes[i] == filename.length()) {
                String fname = new String(filenames[i], 0, filesizes[i]);
                if(fname.equals(filename)) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    public boolean ifree(short iNumber) {
        if(filesizes[iNumber] == 0) {
            return false;
        }
        
        filesizes[iNumber] = 0;
        return true;
    
    }

    
    
}
