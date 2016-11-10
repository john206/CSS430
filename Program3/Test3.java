import java.util.*;

class Test3 extends Thread {

	private int numPairs;								
    private long arrivalTime;								
    private long completionTime;								
    
    public Test3() {
        numPairs = 1;
        SysLib.cout("executing 1 pair (try entering an argument to run more pairs)\n");
    }
    
	public Test3(String[] args) {         
        
        numPairs = Integer.parseInt(args[0]);
        
        // input check: conform number of pairs from 1 to 4
        if (numPairs <= 1) {
            SysLib.cout("executing 1 pair...\n");
            numPairs = 1;
        } 
        else if (numPairs > 4) {
            SysLib.cout("executing 4 pairs...\n"); 
            numPairs = 4;     
        }
        else {
            SysLib.cout("executing " + numPairs + " pairs...\n");
        }
    }

    public void run() {
		
        // start the timer
        arrivalTime = new Date().getTime();				

		String[] testThread3a = SysLib.stringToArgs("TestThread3a");
		String[] testThread3b = SysLib.stringToArgs("TestThread3b");

		// execute cpu bound threads
        for(int i = 0; i < numPairs; i++) { 
            SysLib.exec(testThread3a); 
        }
                     
        // execute i/o bound threads
		for(int i = 0; i < numPairs; i++) { 
            SysLib.exec(testThread3b); 
        }	     
        
        // reap all threads 
		for (int i = 0; i < (2 * numPairs); i++) {
		   	SysLib.join();
		}        
                
        // end the timer
		completionTime = new Date().getTime();				

		SysLib.cout("TAT: " + (completionTime - arrivalTime) + "ms\n");
		SysLib.exit();
    }
}