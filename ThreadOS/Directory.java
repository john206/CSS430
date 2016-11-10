public class Directory {
    private static final int maxChars = 30; 
    private static final int SIZEOF_CHAR = 2;
    private static final int SIZEOF_INT = 4;  
    private static final int ERROR = -1;
    
    private int fsizes[];               
    private char fnames[][];             
    
    public Directory(int maxInumber) {   
        fsizes = new int[maxInumber];    
        
        for(int i = 0; i < maxInumber; i++) {
            fsizes[i] = 0;                
        }
        
        fnames = new char[maxInumber][maxChars];
        String root = "/";               
        fsizes[0] = root.length();        
        root.getChars(0, fsizes[0], fnames[0], 0);  
    }
    
    public void bytes2directory(byte[] data) {
        int offset = 0;
        for (int i = 0; i < fsizes.length; i++, offset += SIZEOF_INT) {
            fsizes[i] = SysLib.bytes2int(data, offset);
        }
        
        for (int i = 0; i < fnames.length; i++, offset += maxChars * SIZEOF_CHAR) {
            String fname = new String(data, offset, maxChars * SIZEOF_CHAR);
            fname.getChars(0, fsizes[i], fnames[i], 0);
        }
    }
    
    public byte[] directory2bytes() {
        byte[] buffer = new byte[fsizes.length * SIZEOF_INT + fnames.length * maxChars * SIZEOF_CHAR];
        int offset = 0;
        
        for (int i = 0; i < fsizes.length; i++, offset += SIZEOF_INT) {
            SysLib.int2bytes(fsizes[i], buffer, offset);
        }
        
        for (int i = 0; i < fnames.length; i++, offset += maxChars * SIZEOF_CHAR) {
            String fname = new String(fnames[i], 0, fsizes[i]);
            byte fileBuffer[] = fname.getBytes();
            
            for(int j = 0; j < fileBuffer.length; j++) {
                buffer[j + offset] = fileBuffer[j];
            }
        }        
        return buffer;
    }
    
    public short ialloc(String filename) {
        for (short i = 1; i < fsizes.length; i++) {
            if(fsizes[i] == 0) {
                int s = filename.length();
                if(s > maxChars) { 
                    s = maxChars;
                }
                
                fsizes[i] = s;
                filename.getChars(0, fsizes[i], fnames[i], 0); 
                return i; 
            }
        }
        return ERROR;        
    }    
    
    public boolean ifree(short iNumber) {
        if(fsizes[iNumber] == 0) {
            return false;
        }
        
        fsizes[iNumber] = 0;
        return true;
    }
    
    public short namei(String filename) {
        for(short i = 0; i < fsizes.length; i++) {
            if(fsizes[i] == filename.length()) {
                String temp = new String(fnames[i], 0, fsizes[i]);
                if(temp.equals(filename)) {
                    return i;
                }
            }
        }        
        return ERROR;
    }
}
