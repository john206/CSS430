class TestThread3b extends Thread {

	private byte[] block;								
    private final int NUM_RW_OPS = 750;
    private final int MAX_BLOCK_SIZE = 512;
    
    public TestThread3b () {}							

    // TestThread3b exists to deliberately eat up a bunch of i/o time.
    // It accomplishes this by performing a large number of reads/writes to disk
    public void run() {
		block = new byte[MAX_BLOCK_SIZE];						
		
        for (int i = 0; i < NUM_RW_OPS; i++) {
			SysLib.rawwrite(i, block);				
			SysLib.rawread(i, block);								
		}
        
		SysLib.exit();
    }
}